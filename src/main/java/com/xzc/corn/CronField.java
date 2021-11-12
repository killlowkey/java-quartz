package com.xzc.corn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ray
 * @date created in 2021/11/9 17:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
class CronField {
    int[] values;

    public boolean isEmpty() {
        return values.length == 0;
    }
}
