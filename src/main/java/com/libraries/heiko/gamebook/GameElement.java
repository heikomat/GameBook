package com.libraries.heiko.gamebook;

import com.libraries.heiko.gamebook.tools.GameStack;

/**
 * Created by heiko on 19.02.2016.
 */
public class GameElement
{
    public String id;                               // The ID of the GameElement
    public GamePage page;                           // Reference to the GamePage that created this element
    public GameBook book;                           // Reference to the GameBook that created this element
    public GameElement parent;                      // Reference to the GameElement that acts as the Parent of this elemente

    public Object value;                            // the current value of the element
    public int x = 0;                             // the x-position of the element
    public int y = 0;                             // the y-position of the element
    public int width = 0;                         // The width of the elemenet
    public int height = 0;                        // The height of the element
    public boolean visible = true;                  // true: The GameElement is visible, false: The GameElement is not visible
    public boolean hideOverflow = false;            // true: childelements visually can't be oudside this element, false: they can

    // cache-variables to prevent memory-allocations
    public GameStack<GAnimation> animations;        // Stack of the currently active animations
    public GameStack<GameElement> children;         // Stack of the child-elements

    // cache-variables to prevent memory-allocations
    private GameStack<GAnimation> drawAnimations;   // used by the Draw function to iterate through the animations
    private GameStack<GameElement> drawElements;    // used by the Draw function to iterate through the child-elements
    private GameStack<GAnimation> tempAnimations;   // used by everything but the Draw function to iterate through the child-elements
    private GameStack<GameElement> tempElements;    // used by everything but the Draw function to iterate through the child-elements
    private GameElement tempElement;                // used to cache a GameElement

    public GameElement(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        this.id = a_id;
        this.page = a_page;
        this.book = a_book;
        this.parent = a_parent;

        this.children = new GameStack<GameElement>();
        this.animations = new GameStack<GAnimation>();
    }

    /*
        Function: AddChild
            Registers a GameElement as a child of this element

        Parameter:
            a_element   - GameElement   | The GameElement to register
    */
    public void AddChild(GameElement a_element)
    {
        this.children.push(a_element);
    }

    /*
        Function: RemoveChild
            Removes a direct child of this GameElement

        Parameter:
            a_id   - String   | The id of the child to remove
    */
    public void RemoveChild(String a_id)
    {
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            if (this.tempElements.content.id.equals(a_id))
            {
                this.tempElements.content.RemoveAllChildren();
                this.tempElements.pop();
                return;
            }
        }
    }

    /*
        Function: RemoveAllChildren
            Removes all child-elements of this element
    */
    public void RemoveAllChildren()
    {
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            this.tempElements.content.RemoveAllChildren();
            this.tempElements.pop();
        }
    }

    /*
        Function: GetElement
            Returns the GameElement with the given ID

        Parameter:
            a_id    - String    | The ID of the element

        Returns:
            GameElement -> - The GameElement with the given ID
    */
    public GameElement GetElement(String a_id)
    {
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            if (this.tempElements.content.id.equals(a_id))
                return this.tempElements.content;

            this.tempElement = this.tempElements.content.GetElement(a_id);
            if (this.tempElement != null)
                return this.tempElement;

            this.tempElements = this.tempElements.next;
        }

        return null;
    }

    /*
        Function: SetValue
            Returns the GameElement with the given ID

        Parameter:
            a_id    - String    | The ID of the element

        Returns:
            GameElement -> - The GameElement with the given ID
    */
    public void SetValue(Object a_value)
    {
        this.value = a_value;
    }

    // Führt frame-updates aus, die für alle Elemente gelten, und update die child-Elemente
    public final void Update(long a_timeDelta, double a_timeFactor)
    {
        this.tempAnimations = this.animations;
        while (this.tempAnimations.content != null)
        {
            this.tempAnimations.content.Update(a_timeDelta, a_timeFactor);
            this.tempAnimations = this.tempAnimations.next;
        }

        this._Update(a_timeDelta, a_timeFactor);
        this.tempElements = this.children;
        while (this.tempElements.content != null)
        {
            this.tempElements.content.Update(a_timeDelta, a_timeFactor);
            this.tempElements = this.tempElements.next;
        }
    }

    // Draws this element and all its child-elements to the framebuffer
    public final void Draw(int a_shaderProgram)
    {
        if (!this.visible)
            return;

        this.drawAnimations = this.animations;
        while (this.drawAnimations.content != null)
        {
            this.drawAnimations.content.Apply(a_shaderProgram);
            this.drawAnimations = this.drawAnimations.next;
        }

        this._Draw(a_shaderProgram);

        if (this.hideOverflow)
            this._ApplyMask(a_shaderProgram);

        // TODO: Achieve the Canvas.translate-effect in OpenGL, so childen can be positioned relatively
        //a_targetCanvas.translate(this.x, this.y);

        this.drawElements = this.children;
        while (this.drawElements.content != null)
        {
            this.drawElements.content.Draw(a_shaderProgram);
            this.drawElements = this.drawElements.next;
        }
    }

    // placeholder for the _Update-function. Can be overwritten by the actual controls
    public void _Update(long a_timeDelta, double a_timeFactor)
    {
    }

    // placeholder for the _Draw-function. Can be overwritten by the actual controls
    public void _Draw(int a_shaderProgram)
    {
    }

    // placeholder for the _ApplyMask-function. Can be overwritten by the actual controls
    public void _ApplyMask(int a_shaderProgram)
    {
    }
}
