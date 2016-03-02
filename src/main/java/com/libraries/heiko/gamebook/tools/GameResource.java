package com.libraries.heiko.gamebook.tools;

/**
 * Created by heiko on 28.02.2016.
 */
public class GameResource
{
    String id;
    Object resource;
    public GameResource(String a_id)
    {
        this.id = a_id;
    }

    public GameResource(String a_id, Object a_resource)
    {
        this.id = a_id;
        this.resource = a_resource;
    }
}
