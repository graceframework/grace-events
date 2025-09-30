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

import java.util.concurrent.atomic.AtomicInteger

import spock.lang.Specification

import grails.events.bus.EventBusAware
import org.grails.events.transform.AnnotatedSubscriber

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class PubSubSpec extends Specification {

    void "test pub/sub with default event bus"() {
        given:
        SumService sumService = new SumService()
        TotalService totalService = new TotalService()
        AnnotatedSubscriber annotatedSubscriber = (AnnotatedSubscriber) totalService
        EventBusAware publisher = (EventBusAware) sumService
        annotatedSubscriber.setTargetEventBus(publisher.getEventBus())
        annotatedSubscriber.registerMethods()

        when:
        sumService.sum(1, 2)
        sumService.sum(1, 2)

        then:
        totalService.total.intValue() == 6
    }

}

// tag::publisher[]
class SumService {

    @Publisher
    int sum(int a, int b) {
        a + b
    }

}
// end::publisher[]

// tag::subscriber[]
class TotalService {

    AtomicInteger total = new AtomicInteger(0)

    @Subscriber
    void onSum(int num) {
        total.addAndGet(num)
    }

}
// end::subscriber[]
