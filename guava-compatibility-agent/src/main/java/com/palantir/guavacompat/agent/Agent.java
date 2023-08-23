/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.guavacompat.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

public final class Agent {

    private static final String OBJECTS = "com.google.common.base.Objects";
    private static final String MORE_OBJECTS = "com.google.common.base.MoreObjects";
    private static final String FUTURES = "com.google.common.util.concurrent.Futures";
    private static final String LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture";
    private static final String GUAVA_FUNCTION = "com.google.common.base.Function";
    private static final String ASYNC_FUNCTION = "com.google.common.util.concurrent.AsyncFunction";
    private static final String MORE_EXECUTORS = "com.google.common.util.concurrent.MoreExecutors";
    private static final String LISTENING_EXECUTOR = "com.google.common.util.concurrent.ListeningExecutorService";

    @SuppressWarnings("checkstyle:MethodLength")
    public static void premain(String _args, Instrumentation instrumentation) {
        Class<?> moreObjectsClass = loadNullable(MORE_OBJECTS);
        if (moreObjectsClass == null) {
            // new guava is not present, we should not attempt to route old invocations to new guava.
            return;
        }
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .type(ElementMatchers.named(OBJECTS))
                .transform((in, _type, _classLoader, _module, _protection) -> {
                    DynamicType.Builder<?> builder = in;
                    // Objects.firstNonNull(first,second) -> MoreObjects.firstNonNull(first,second)
                    TypeDescription.Generic typeVariable =
                            TypeDescription.Generic.Builder.typeVariable("T").build();
                    builder = builder.defineMethod("firstNonNull", typeVariable, Modifier.PUBLIC | Modifier.STATIC)
                            .withParameter(typeVariable, "first")
                            .withParameter(typeVariable, "second")
                            .typeVariable(typeVariable.getSymbol())
                            .intercept(MethodDelegation.to(moreObjectsClass));
                    // TODO(ckozak): Handle the following:
                    // Objects.toStringHelper(Object) -> MoreObjects.toStringHelper(Object)
                    // Objects.toStringHelper(Class) -> MoreObjects.toStringHelper(Class)
                    // Objects.toStringHelper(String) -> MoreObjects.toStringHelper(String)
                    return builder;
                });

        agentBuilder = agentBuilder
                .type(ElementMatchers.named(FUTURES))
                .transform((builder, _type, _classLoader, _module, _protection) -> {
                    TypeDescription.Generic inputTypeVariable =
                            TypeDescription.Generic.Builder.typeVariable("I").build();
                    TypeDescription.Generic outputTypeVariable =
                            TypeDescription.Generic.Builder.typeVariable("O").build();
                    TypeDescription.Latent listenableFuture =
                            new TypeDescription.Latent(
                                    LISTENABLE_FUTURE,
                                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE,
                                    null,
                                    TypeDescription.Generic.Builder.of(Future.class)
                                            .build()) {
                                @Override
                                public TypeDescription getDeclaringType() {
                                    return null;
                                }

                                @Override
                                public AnnotationList getDeclaredAnnotations() {
                                    return new AnnotationList.Empty();
                                }

                                @Override
                                public TypeList.Generic getTypeVariables() {
                                    return new TypeList.Generic.Explicit(
                                            TypeDescription.Generic.Builder.typeVariable("V")
                                                    .build());
                                }
                            };
                    TypeDescription.Latent guavaFunction =
                            new TypeDescription.Latent(
                                    GUAVA_FUNCTION,
                                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE,
                                    null,
                                    TypeDescription.Generic.Builder.of(java.util.function.Function.class)
                                            .build()) {
                                @Override
                                public TypeDescription getDeclaringType() {
                                    return null;
                                }

                                @Override
                                public AnnotationList getDeclaredAnnotations() {
                                    return new AnnotationList.Empty();
                                }

                                @Override
                                public TypeList.Generic getTypeVariables() {
                                    return new TypeList.Generic.Explicit(
                                            TypeDescription.Generic.Builder.typeVariable("F")
                                                    .build(),
                                            TypeDescription.Generic.Builder.typeVariable("T")
                                                    .build());
                                }
                            };
                    TypeDescription.Latent asyncFunction =
                            new TypeDescription.Latent(
                                    ASYNC_FUNCTION,
                                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE,
                                    null) {
                                @Override
                                public TypeDescription getDeclaringType() {
                                    return null;
                                }

                                @Override
                                public AnnotationList getDeclaredAnnotations() {
                                    return new AnnotationList.Empty();
                                }

                                @Override
                                public TypeList.Generic getTypeVariables() {
                                    return new TypeList.Generic.Explicit(
                                            TypeDescription.Generic.Builder.typeVariable("I")
                                                    .build(),
                                            TypeDescription.Generic.Builder.typeVariable("O")
                                                    .build());
                                }
                            };

                    TypeDescription.Latent moreExecutors =
                            new TypeDescription.Latent(
                                    MORE_EXECUTORS, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, null) {
                                @Override
                                public TypeDescription getDeclaringType() {
                                    return null;
                                }

                                @Override
                                public AnnotationList getDeclaredAnnotations() {
                                    return new AnnotationList.Empty();
                                }

                                @Override
                                public TypeList.Generic getTypeVariables() {
                                    return new TypeList.Generic.Empty();
                                }
                            };

                    MethodCall.WithoutSpecifiedTarget invokeDirectExecutor =
                            MethodCall.invoke(new MethodDescription.Latent(
                                    moreExecutors,
                                    new MethodDescription.Token(
                                            "directExecutor",
                                            Modifier.PUBLIC | Modifier.STATIC,
                                            TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(
                                                    Executor.class))));
                    Generic returnType = TypeDescription.Generic.Builder.parameterizedType(
                                    listenableFuture, outputTypeVariable)
                            .build();
                    return builder
                            // transform(future, function) -> transform(future, function, executor)
                            .defineMethod("transform", returnType, Modifier.PUBLIC | Modifier.STATIC)
                            .withParameter(
                                    TypeDescription.Generic.Builder.parameterizedType(
                                                    listenableFuture, inputTypeVariable)
                                            .build(),
                                    "input")
                            .withParameter(
                                    TypeDescription.Generic.Builder.parameterizedType(
                                                    guavaFunction, Arrays.asList(inputTypeVariable, outputTypeVariable))
                                            .build(),
                                    "function")
                            .typeVariable(inputTypeVariable.getSymbol())
                            .typeVariable(outputTypeVariable.getSymbol())
                            .intercept(MethodCall.invoke(ElementMatchers.named("transform")
                                            .and(ElementMatchers.isPublic())
                                            .and(ElementMatchers.isStatic())
                                            .and(ElementMatchers.takesArguments(
                                                    listenableFuture,
                                                    guavaFunction,
                                                    TypeDescription.ForLoadedType.of(Executor.class))))
                                    .with(
                                            new MethodCall.ArgumentLoader.ForMethodParameter.Factory(0),
                                            new MethodCall.ArgumentLoader.ForMethodParameter.Factory(1))
                                    .withMethodCall(invokeDirectExecutor))
                            // transform(future, asyncfun) -> transformAsync(future, asyncfun, executor)
                            .defineMethod("transform", returnType, Modifier.PUBLIC | Modifier.STATIC)
                            .withParameter(
                                    TypeDescription.Generic.Builder.parameterizedType(
                                                    listenableFuture, inputTypeVariable)
                                            .build(),
                                    "input")
                            .withParameter(
                                    TypeDescription.Generic.Builder.parameterizedType(
                                                    asyncFunction, Arrays.asList(inputTypeVariable, outputTypeVariable))
                                            .build(),
                                    "function")
                            .typeVariable(inputTypeVariable.getSymbol())
                            .typeVariable(outputTypeVariable.getSymbol())
                            .intercept(MethodCall.invoke(ElementMatchers.named("transformAsync")
                                            .and(ElementMatchers.isPublic())
                                            .and(ElementMatchers.isStatic())
                                            .and(ElementMatchers.takesArguments(
                                                    listenableFuture,
                                                    asyncFunction,
                                                    TypeDescription.ForLoadedType.of(Executor.class))))
                                    .with(
                                            new MethodCall.ArgumentLoader.ForMethodParameter.Factory(0),
                                            new MethodCall.ArgumentLoader.ForMethodParameter.Factory(1))
                                    .withMethodCall(invokeDirectExecutor))
                            // transform(future, asyncfun, executor) -> transformAsync(future, asyncfun, executor)
                            .defineMethod("transform", returnType, Modifier.PUBLIC | Modifier.STATIC)
                            .withParameter(
                                    TypeDescription.Generic.Builder.parameterizedType(
                                                    listenableFuture, inputTypeVariable)
                                            .build(),
                                    "input")
                            .withParameter(
                                    TypeDescription.Generic.Builder.parameterizedType(
                                                    asyncFunction, Arrays.asList(inputTypeVariable, outputTypeVariable))
                                            .build(),
                                    "function")
                            .withParameter(Executor.class, "executor")
                            .typeVariable(inputTypeVariable.getSymbol())
                            .typeVariable(outputTypeVariable.getSymbol())
                            .intercept(MethodCall.invoke(ElementMatchers.named("transformAsync")
                                            .and(ElementMatchers.isPublic())
                                            .and(ElementMatchers.isStatic())
                                            .and(ElementMatchers.takesArguments(
                                                    listenableFuture,
                                                    asyncFunction,
                                                    TypeDescription.ForLoadedType.of(Executor.class))))
                                    .withAllArguments());
                });
        // MoreExecutors.sameThreadExecutor() -> MoreExecutors.newDirectExecutorService()
        agentBuilder = agentBuilder
                .type(ElementMatchers.named(MORE_EXECUTORS))
                .transform((builder, _type, _classLoader, _module, _protection) -> {
                    TypeDescription.Latent listeningExecutor =
                            new TypeDescription.Latent(
                                    LISTENING_EXECUTOR,
                                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE,
                                    null,
                                    TypeDescription.Generic.Builder.of(ExecutorService.class)
                                            .build()) {
                                @Override
                                public TypeDescription getDeclaringType() {
                                    return null;
                                }

                                @Override
                                public AnnotationList getDeclaredAnnotations() {
                                    return new AnnotationList.Empty();
                                }

                                @Override
                                public TypeList.Generic getTypeVariables() {
                                    return new TypeList.Generic.Empty();
                                }
                            };
                    return builder.defineMethod(
                                    "sameThreadExecutor", listeningExecutor, Modifier.PUBLIC | Modifier.STATIC)
                            .intercept(MethodCall.invoke(ElementMatchers.named("newDirectExecutorService")
                                    .and(ElementMatchers.isStatic())
                                    .and(ElementMatchers.isPublic())
                                    .and(ElementMatchers.takesNoArguments())));
                });

        // TODO(ckozak): Iterators.emptyIterator() -> public
        // Could use a value resulting from ImmutableList.<T>of().listIterator()

        agentBuilder.installOn(instrumentation);
    }

    private static Class<?> loadNullable(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private Agent() {}
}
