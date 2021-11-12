package com.xzc;

/**
 * @author Ray
 * @date created in 2021/11/7 9:03
 */
public interface Trigger {

    /**
     * Trigger description
     *
     * @return Trigger description
     */
    String description();

    /**
     * next schedule time
     *
     * @param prev prev run time
     * @return next schedule time
     */
    long nextFireTime(long prev);

    Context context();

    void setContext(Context context);
}
