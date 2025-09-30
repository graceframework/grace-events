/*
 * Copyright 2017-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.events.annotation

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import grails.events.Event
import grails.events.bus.EventBusBuilder
import grails.gorm.transactions.Transactional
import org.grails.datastore.mapping.simple.SimpleMapDatastore

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class PublishSubscribeSpringSpec extends Specification {

    @Shared
    @AutoCleanup
    SimpleMapDatastore datastore = new SimpleMapDatastore()

    def "test event publisher within Spring"() {
        given:
        def conditions = new PollingConditions(timeout: 5, delay: 0.2)

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
        applicationContext.beanFactory.registerSingleton('eventBus', new EventBusBuilder().build())
        applicationContext.register(OneService, TwoService)
        applicationContext.refresh()
        OneService publisher = applicationContext.getBean(OneService)
        TwoService subscriber = applicationContext.getBean(TwoService)

        when:
        publisher.sum(1, 2)

        then:
        conditions.eventually {
            subscriber.error == null
            subscriber.total == 3
            subscriber.events.size() == 0
            // subscriber.events[0].parameters == [a:1,b:2]
            subscriber.transactionalInvoked
        }

        when:
        publisher.wrongType()

        then:
        conditions.eventually {
            subscriber.total == 3
            subscriber.events.size() == 2
            subscriber.error == null
        }

        when:
        publisher.badSum(1, 2)

        then:
        def e = thrown(RuntimeException)
        conditions.eventually {
            assert e.message == 'bad'
            assert subscriber.error == e
            assert subscriber.events.size() == 0
            assert subscriber.total == 3
        }
    }

}

@Component
class OneService {

    @Publisher
    int sum(int a, int b) {
        a + b
    }

    @Publisher('sum')
    int badSum(int a, int b) {
        throw new RuntimeException('bad')
    }

    @Publisher('sum')
    Date wrongType() {
        new Date()
    }

}

@Component
class TwoService {

    int total = 0
    List<Event> events = []
    boolean transactionalInvoked = false
    Throwable error

    @Subscriber
    void onSum(int num) {
        total += num
    }

    @Subscriber
    void onSum(Throwable t) {
        error = t
    }

    @Subscriber('sum')
    void onSum2(Event event) {
        events.add(event)
    }

    @Subscriber('sum')
    @Transactional
    void doSomething(Event event) {
        transactionStatus != null
        transactionalInvoked = true
    }

}
