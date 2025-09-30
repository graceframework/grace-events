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

import spock.lang.Specification

import grails.events.bus.EventBus
import grails.events.subscriber.MethodSubscriber
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.grails.events.gorm.GormAnnotatedSubscriber
import org.grails.events.transform.AnnotatedSubscriber

/**
 * @author Graeme Rocher
 * @since 3.3
 */
class SubscriberSpec extends Specification {

    void "test subscriber transform"() {
        given:
        def service = new GroovyClassLoader().parseClass('''
class TestService {
    int total = 0
    @grails.events.annotation.Subscriber('total')
    void onSum(int num) {
        total += num
    }
}

''').newInstance()
        def methodObject = service.getClass().getDeclaredMethod('onSum', int)

        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus

        then:
        ClassPropertyFetcher.forClass(service.getClass()).getPropertyValue('lazyInit') == false
        service instanceof AnnotatedSubscriber

        when:
        service.registerMethods()

        then:
        1 * eventBus.subscribe('total', new MethodSubscriber(service, methodObject))
    }

    void "test gorm event subscriber transform"() {
        given:
        def service = new GroovyShell().evaluate('''
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import grails.events.annotation.*

class TestService {
    @Subscriber
    void onInsert(PreInsertEvent event) {
        // whatever
    }
}
return TestService
''').newInstance()
        def methodObject = service.getClass().getDeclaredMethod('onInsert', PreInsertEvent)

        when:
        def eventBus = Mock(EventBus)
        service.targetEventBus = eventBus

        then:
        ClassPropertyFetcher.forClass(service.getClass()).getPropertyValue('lazyInit') == false
        service instanceof GormAnnotatedSubscriber

        when:
        service.registerMethods()

        then:
        1 * eventBus.subscribe('gorm:preInsert', new MethodSubscriber(service, methodObject))
    }

}
