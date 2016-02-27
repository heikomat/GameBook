package com.libraries.heiko.gamebook.tools;

/**
 * Created by heiko on 20.02.2016.
 */
public class GameStack <T>
{
    private GameStack<T> temp = null;
    private int counter = 0;

    public GameStack<T> next = null;
    public T content = null;

    public void push(T a_content)
    {
        this.temp = new GameStack<T>();
        this.temp.content = this.content;
        this.temp.next = this.next;
        this.content = a_content;
        this.next = this.temp;
    }

    public T pop()
    {
        T cache = this.content;
        if (this.next == null)
            this.content = null;
        else
        {
            this.content = this.next.content;
            this.next = this.next.next;
        }

        return cache;
    }

    public T peek()
    {
        return this.content;
    }
}
