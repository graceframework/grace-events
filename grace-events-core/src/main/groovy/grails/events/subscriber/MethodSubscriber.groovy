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
package grails.events.subscriber

import java.lang.reflect.Method

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.util.ReflectionUtils

/**
 * Invokes a method to trigger an event
 *
 * @author Graeme Rocher
 * @since 3.3
 */
@CompileStatic
@Slf4j
@EqualsAndHashCode(includes = ['target', 'method'])
@ToString(includes = ['method'])
class MethodSubscriber implements Subscriber<Object, Object> {

    final Object target
    final Method method
    final Class[] parameterTypes
    final int parameterLength

    ConversionService conversionService = new DefaultConversionService()

    MethodSubscriber(Object target, Method method) {
        this.target = target
        this.method = method
        this.parameterTypes = method.parameterTypes
        this.parameterLength = parameterTypes.length

        if (target.getClass() != method.getDeclaringClass()) {
            throw new IllegalArgumentException("The target must be an instance of the declaring class for method $method")
        }
    }

    @Override
    Object call(Object arg) {
        switch (parameterLength) {
            case 0:
                return ReflectionUtils.invokeMethod(method, target)
            case 1:
                Class parameterType = parameterTypes[0]
                if (parameterType.isInstance(arg)) {
                    return ReflectionUtils.invokeMethod(method, target, arg)
                }
                def converted = conversionService.canConvert(arg.getClass(), parameterType)
                        ? conversionService.convert(arg, parameterType as Class<Object>) : null
                if (converted == null) {
                    log.debug("Could not convert Event argument [$arg] to required type to invoke listener [$method]. Ignoring.")
                    break
                }
                return ReflectionUtils.invokeMethod(method, target, converted)
            default:
                if (arg != null && arg.getClass().isArray()) {
                    Object[] array = (Object[]) arg

                    if (array.length != parameterLength) {
                        log.debug("Could not convert Event argument [$arg] to required type to invoke listener [$method]. Ignoring.")
                        break
                    }
                    Object[] converted = new Object[array.length]
                    int i = 0
                    for (o in array) {
                        Class parameterType = parameterTypes[i]
                        if (parameterType.isInstance(o)) {
                            converted[i] = array[i]
                        } else {
                            converted[i] = conversionService.convert(o, parameterType)
                        }
                        i++
                    }
                    return ReflectionUtils.invokeMethod(method, target, converted)
                }
                log.debug("Could not convert Event argument [$arg] to required type to invoke listener [$method]. Ignoring.")
                break
        }
    }

}
