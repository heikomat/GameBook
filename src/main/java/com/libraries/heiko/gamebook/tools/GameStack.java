package com.libraries.heiko.gamebook.tools;

/**
 * Created by heiko on 20.02.2016.
 */
public class GameStack <T>
{
    private GameStack<T> temp = null;   // used when pushing a new item
    public GameStack<T> next = null;    // stores a reference to the next item
    public T content = null;            // stores the content

    /*
        Function: push
            Pushes a new Item to the top of the stack

        Parameter:
            a_content   - T | The content to store
    */
    public void push(T a_content)
    {
        this.temp = new GameStack<T>();
        this.temp.content = this.content;
        this.temp.next = this.next;
        this.content = a_content;
        this.next = this.temp;
    }

    /*
        Function: pop
            Removes the item at the top of the stack

        Returns:
            T -> - The item that was just removed from the stack
    */
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

    /*
        Function: peek
            Gets the item at the top of the stack

        Returns:
            T -> - The item currently at the top of the stack
    */
    public T peek()
    {
        return this.content;
    }
}
