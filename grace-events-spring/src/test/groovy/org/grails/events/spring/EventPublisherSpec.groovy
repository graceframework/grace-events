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
package org.grails.events.spring

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import spock.lang.Specification

import grails.events.EventPublisher

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class EventPublisherSpec extends Specification {

    def "test event publisher within Spring"() {
        given:
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()
        def bus = new SpringEventBus(applicationContext)
        applicationContext.beanFactory.registerSingleton('eventBus', bus)
        applicationContext.register(MyPublisher)
        applicationContext.refresh()

        when:
        MyPublisher publisher = applicationContext.getBean(MyPublisher)
        def result
        bus.on('test') {
            result = "good $it"
        }
        publisher.publish('test', 'data')

        then:
        result == 'good data'
    }

}

@Component
class MyPublisher implements EventPublisher {

}
