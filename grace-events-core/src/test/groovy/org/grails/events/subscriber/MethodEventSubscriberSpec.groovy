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
package org.grails.events.subscriber

import spock.lang.Specification

import grails.events.subscriber.MethodSubscriber

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class MethodEventSubscriberSpec extends Specification {

    void "test convert method argument"() {
        given:
        TestService testService = new TestService()
        def subscriber = new MethodSubscriber(testService, TestService.getMethod('foo', Integer))

        expect:
        subscriber.call(1) == 2
        subscriber.call('1') == 2
        subscriber.call('') == null
    }

}

class TestService {

    def foo(Integer num) {
        return num + 1
    }

}
