#!/usr/bin/env groovy

/*
 ** Data types helper.
 * This module is meant for data type helpers that usually make it easier to convert
 * between data types.
 *
 */

/**
 ** Function:
 * Convert a LazyMap to a Map. This was originally needed to deal with the LazyMaps
 * that you get when you parse JSON through JsonSlurper.
 *
 ** Parameters:
 * @param LazyMap lazyMap
 *
 * @return Map
 */
def lazyMapToMap(def lazyMap) {
    def map = [:]
    for (entry in lazyMap) {
        map[entry.key] = entry.value
    }
    return map
}

// Note: this line is crucial when you want to load an external groovy script
return this