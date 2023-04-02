package com.matzip.server.global.config;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class DialectConfig extends MySQL8Dialect {
    public DialectConfig() {
        super();

        registerFunction("match", new SQLFunctionTemplate(
                StandardBasicTypes.DOUBLE, "MATCH(?1) AGAINST (?2 in boolean mode)"));
    }
}
