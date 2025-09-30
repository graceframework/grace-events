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
package org.grails.events

import spock.lang.Specification

import org.grails.events.bus.ExecutorEventBus

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class TaskExecutorEventBusSpec extends Specification {

    void 'test task executor event bus single arg'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        def result
        eventBus.on('test') {
            result = "foo $it"
        }
        eventBus.notify('test', 'bar')

        expect:
        result == 'foo bar'
    }

    void 'test task executor event bus multiple args'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        def result
        eventBus.on('test') {
            result = "foo $it"
        }
        eventBus.notify('test', 'bar', 'baz')

        expect:
        result == 'foo [bar, baz]'
    }

    void 'test task executor event bus multiple args listener'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        def result
        eventBus.on('test') { String one, String two ->
            result = "foo $one $two"
        }
        eventBus.notify('test', 'bar', 'baz')

        expect:
        result == 'foo bar baz'
    }

    void 'test task executor event bus send and receive'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        def result
        eventBus.on('test') { String data ->
            "foo $data"
        }
        eventBus.sendAndReceive('test', 'bar') {
            result = it
        }

        expect:
        result == 'foo bar'
    }

    void 'test task executor bus error handling'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        def result
        eventBus.on('test') { String data ->
            throw new RuntimeException('bad')
        }
        eventBus.sendAndReceive('test', 'bar') {
            result = it
        }

        expect:
        result instanceof Throwable
    }

    void 'test task executor bus error handling with publish'() {
        given:
        ExecutorEventBus eventBus = new ExecutorEventBus()
        when:
        def result
        eventBus.on('test') { String data ->
            throw new RuntimeException('bad')
        }
        eventBus.publish('test', 'bar')

        then:
        thrown(RuntimeException)
    }

}
