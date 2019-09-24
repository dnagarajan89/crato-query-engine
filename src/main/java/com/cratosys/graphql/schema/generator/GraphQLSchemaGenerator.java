package com.cratosys.query.engine;

import com.cratosys.data.model.Invoice;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphQLSchemaGenerator {

    private static List<Class<?>> intClassTypes = Arrays.asList(int.class, long.class, short.class, Integer.class, Long.class, Short.class, BigInteger.class);

    private static List<Class<?>> floatClassTypes = Arrays.asList(float.class, double.class, Float.class, Double.class, BigDecimal.class);

    private static List<Class<?>> collectionClassTypes = Arrays.asList(List.class, Set.class);

    private static Set<Class<?>> processedTypes = new HashSet<>();

    public static String filePath = "D:\\Projects\\crato-query-engine\\src\\main\\resources\\schemagen.graphqls";

    public static void main(String[] args) throws Exception {
        processedTypes.clear();

        Set<Class<?>> typeClasses = new HashSet<>();

        StringBuilder querySchemaBuilder = new StringBuilder();
        querySchemaBuilder
                .append("scalar LocalDateTime\n\n");
        querySchemaBuilder
                .append("type Query {\n");
        for(Method method : QuerySchema.class.getMethods()) {
            querySchemaBuilder
                    .append("\t")
                    .append(method.getName())
                    .append("(");
            int iteration = 1;
            for(Parameter parameter: method.getParameters()) {
                if(iteration > 1) {
                    querySchemaBuilder.append(", ");
                }
                querySchemaBuilder.append(parameter.getName())
                        .append(": ")
                        .append(parameter.getType().getSimpleName());
                iteration++;
            }
            querySchemaBuilder.append("): ");
            if(collectionClassTypes.contains(method.getReturnType())) {
                Type returnType = method.getGenericReturnType();
                if(returnType instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) returnType;
                    Class<?> genericType = (Class) type.getActualTypeArguments()[0];
                    querySchemaBuilder
                            .append("[")
                            .append(genericType.getSimpleName())
                            .append("]");
                    if(!typeClasses.contains(genericType)) {
                        typeClasses.add(genericType);
                    }

                }
            } else {
                querySchemaBuilder.append(method.getReturnType().getSimpleName());
                if(!typeClasses.contains(method.getReturnType())) {
                    typeClasses.add(method.getReturnType());
                }
            }
            querySchemaBuilder.append("\n");
        }
        querySchemaBuilder
                .append("}\n");
        Files.deleteIfExists(Paths.get(filePath));
        Files.write(Paths.get(filePath), querySchemaBuilder.toString().getBytes());
        System.out.println(querySchemaBuilder.toString());
        typeClasses.forEach(GraphQLSchemaGenerator::generateSchemaForClass);
    }

    public static void generateSchemaForClass(Class clazz) {
        if(processedTypes.contains(clazz)) {
            return;
        }
        StringBuilder schemaBuilder = new StringBuilder();
        Set<Class<?>> subClasses = new HashSet<>();
        schemaBuilder
                .append("type ")
                .append(clazz.getSimpleName())
                .append(" {")
                .append("\n");
        for(Field field : clazz.getDeclaredFields()) {
            GraphQLTypeData graphQLTypeData = getGraphQLTypeData(field);
            if(graphQLTypeData.isUnsupportedType) {
                continue;
            }
            schemaBuilder
                    .append("\t")
                    .append(field.getName())
                    .append(": ");
            if(graphQLTypeData.isCollection) {
                schemaBuilder.append("[");
            }
            if(graphQLTypeData.isNativeType) {
                schemaBuilder.append(graphQLTypeData.getGraphQLType().name());
            } else {
                schemaBuilder.append(graphQLTypeData.getObjectType().getSimpleName());
                if(!processedTypes.contains(graphQLTypeData.getObjectType())) {
                    subClasses.add(graphQLTypeData.getObjectType());
                }
            }
            if(graphQLTypeData.isCollection) {
                schemaBuilder.append("]");
            }
            schemaBuilder.append("\n");
        }
        schemaBuilder.append("}");
        schemaBuilder.append("\n\n");
        try {
            Files.write(Paths.get(filePath), schemaBuilder.toString().getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        processedTypes.add(clazz);
        if(subClasses.size() == 0) {
            return;
        } else {
            subClasses.forEach(GraphQLSchemaGenerator::generateSchemaForClass);
        }
    }

    public static GraphQLTypeData getGraphQLTypeData(Field field) {
        if (collectionClassTypes.contains(field.getType())) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Class<?> genericType = parameterizedType.getActualTypeArguments().length > 0 ? (Class<?>) parameterizedType.getActualTypeArguments()[0] : null;
            if (genericType == null) {
                GraphQLTypeData graphQLTypeData = new GraphQLTypeData();
                graphQLTypeData.setUnsupportedType(true);
                return graphQLTypeData;
            } else {
                GraphQLTypeData graphQLTypeData = getGraphQLTypeData(genericType);
                graphQLTypeData.setCollection(true);
                return graphQLTypeData;
            }
        } else {
            return getGraphQLTypeData(field.getType());
        }
    }

    private static GraphQLTypeData getGraphQLTypeData(Class<?> fieldType) {
        if (fieldType.equals(String.class) || fieldType.isEnum()) {
            return new GraphQLTypeData(GraphQLType.String, true, fieldType.isArray());
        } else if (intClassTypes.contains(fieldType)) {
            return new GraphQLTypeData(GraphQLType.Int, true, fieldType.isArray());
        } else if (floatClassTypes.contains(fieldType)) {
            return new GraphQLTypeData(GraphQLType.Float, true, fieldType.isArray());
        } else if (fieldType.equals(Boolean.class)) {
            return new GraphQLTypeData(GraphQLType.Boolean, true, fieldType.isArray());
        } else if (fieldType.equals(LocalDateTime.class)) {
            return new GraphQLTypeData(GraphQLType.LocalDateTime, true, fieldType.isArray());
        } else {
            if (fieldType.getName().contains("com.cratosys.data.model")) {
                return new GraphQLTypeData(false, fieldType, fieldType.isArray());
            } else {
                GraphQLTypeData graphQLTypeData = new GraphQLTypeData(false, fieldType, fieldType.isArray());
                graphQLTypeData.setUnsupportedType(true);
                return graphQLTypeData;
            }
        }
    }

    @Getter
    @Setter
    private static class GraphQLTypeData {
        private GraphQLType graphQLType;
        private boolean isNativeType;
        private boolean isCollection;
        private Class<?> objectType;
        private boolean isUnsupportedType;

        public GraphQLTypeData() {
            super();
        }

        public GraphQLTypeData(GraphQLType graphQLType, boolean isNativeType) {
            this.graphQLType = graphQLType;
            this.isNativeType = isNativeType;
        }

        public GraphQLTypeData(GraphQLType graphQLType, boolean isNativeType, boolean isCollection) {
            this.graphQLType = graphQLType;
            this.isNativeType = isNativeType;
            this.isCollection = isCollection;
        }

        public GraphQLTypeData(boolean isNativeType, Class<?> objectType) {
            this.isNativeType = isNativeType;
            this.objectType = objectType;
        }

        public GraphQLTypeData(boolean isNativeType, Class<?> objectType, boolean isCollection) {
            this.isNativeType = isNativeType;
            this.objectType = objectType;
            this.isCollection = isCollection;
        }
    }

    private enum GraphQLType {
        LocalDateTime,
        Int,
        Float,
        String,
        Boolean
    }
}
