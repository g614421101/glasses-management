package com.glasses.service;

import com.glasses.util.Result;

import java.util.Map;

public interface RecycleBinService {
    Map<String, Object> list(String type);

    Result<Boolean> restore(String type, Long id);

    Result<Boolean> purge(String type, Long id);

    Map<String, Integer> purgeExpired();
}
