package com.github.starnowski.posmulten.postgresql.core

import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.api.Randomizer
import org.junit.Assert
import spock.lang.Specification

import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.stream.Stream

import static java.util.stream.Collectors.toList

class DefaultFunctionDefinitionTest extends Specification {

    def "should create object based on passed object of type that implements IFunctionDefinition" ()
    {
        given:
            RandomString randomString = new RandomString(12, new Random(), RandomString.lower)
            EasyRandomParameters easyRandomParameters = new EasyRandomParameters()
                    .randomize(IFunctionArgument.class, new Randomizer<IFunctionArgument>() {

                IFunctionArgument getRandomValue() {
                    new TestFunctionArgument(randomString.nextString())
                }
            })
            EasyRandom easyRandom = new EasyRandom(easyRandomParameters)
            IFunctionDefinition passedObject = easyRandom.nextObject(DefaultFunctionDefinition)
            List<Method> publicMethods = returnPublicMethodsForInterface(IFunctionDefinition.class)

        when:
            def result = new DefaultFunctionDefinition(passedObject)

        then:
            result

        and: "all methods declared for interface IFunctionDefinition  for passed object and for the newly created object of type DefaultFunctionDefinition should return the same results"
            publicMethods.each {method ->
                Object resultValue =  method.invoke(result)
                Object passedObjectValue =  method.invoke(passedObject)
                Assert.assertEquals("values for method " + method.getName() + " does not match", resultValue, passedObjectValue)
            }
    }

    protected Method[] returnPublicMethodsForInterface(Class aClass)
    {
        Stream.of(aClass.getDeclaredMethods()).filter({ method ->
            Modifier.isPublic(method.getModifiers())
        }).collect(toList())
    }

    private static class TestFunctionArgument implements IFunctionArgument{

        private String type;

        TestFunctionArgument(String type) {
            this.type = type
        }

        @Override
        String getType() {
            return type
        }
    }
}
