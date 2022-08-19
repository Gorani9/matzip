package com.matzip.server;


import org.springframework.util.LinkedMultiValueMap;

import java.util.Collections;

public class Parameters extends LinkedMultiValueMap<String, String> {
    public Parameters() {
        super();
        this.putParameter("pageNumber", "0").putParameter("pageSize", "15");
    }

    public Parameters putParameter(String key, String value) {
        this.put(key, Collections.singletonList(value));
        return this;
    }
}
