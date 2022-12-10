package com.matzip.server;


import org.springframework.util.LinkedMultiValueMap;

import java.util.Collections;

public class Parameters extends LinkedMultiValueMap<String, String> {
    public Parameters() {
        super();
    }

    public Parameters(int page, int size) {
        super();
        this.putParameter("page", String.valueOf(page)).putParameter("size", String.valueOf(size));
    }

    public Parameters putParameter(String key, String value) {
        this.put(key, Collections.singletonList(value));
        return this;
    }
}
