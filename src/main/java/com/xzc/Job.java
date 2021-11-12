package com.xzc;

/**
 * task abstract
 *
 * @author Ray
 * @date created in 2021/11/7 9:00
 */
public interface Job {

    void execute();

    String description();

    int key();

    Context context();

}
