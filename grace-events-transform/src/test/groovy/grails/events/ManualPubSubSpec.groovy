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
package grails.events

import java.util.concurrent.atomic.AtomicInteger

import jakarta.annotation.PostConstruct
import spock.lang.Specification

import grails.events.bus.EventBusAware

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class ManualPubSubSpec extends Specification {

    void "test pub/sub with default event bus"() {
        given:
        SumService sumService = new SumService()
        TotalService totalService = new TotalService()
        EventBusAware annotatedSubscriber = (EventBusAware) totalService
        EventBusAware publisher = (EventBusAware) sumService
        annotatedSubscriber.setTargetEventBus(publisher.getEventBus())
        totalService.init()

        when:
        sumService.sum(1, 2)
        sumService.sum(1, 2)

        then:
        totalService.total.intValue() == 6
    }

}

// tag::publisher[]
class SumService implements EventPublisher {

    int sum(int a, int b) {
        int result = a + b
        notify('sum', result)
        return result
    }

}
// end::publisher[]

// tag::subscriber[]
class TotalService implements EventBusAware {

    AtomicInteger total = new AtomicInteger(0)

    @PostConstruct
    void init() {
        eventBus.subscribe('sum') { int num ->
            total.addAndGet(num)
        }
    }

}
// end::subscriber[]
