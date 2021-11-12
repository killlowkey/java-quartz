package com.xzc.trigger;

import com.xzc.Context;
import com.xzc.Trigger;

import static com.xzc.util.CommonUtil.isEmpty;

/**
 * @author Ray
 * @date created in 2021/11/7 10:31
 */
public abstract class AbstractTrigger implements Trigger {

    private final String description;
    private Context context;

    AbstractTrigger(String description) {
        if (isEmpty(description)) {
            throw new IllegalStateException("description must not be null");
        }

        this.description = description;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public Context context() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
