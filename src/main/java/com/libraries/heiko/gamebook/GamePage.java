package com.libraries.heiko.gamebook;

import android.opengl.GLES20;

import com.libraries.heiko.gamebook.controls.Label;
import com.libraries.heiko.gamebook.controls.Sheet;
import com.libraries.heiko.gamebook.tools.GameFont;
import com.libraries.heiko.gamebook.tools.GameStack;

/**
 * Created by heiko on 19.02.2016.
 */
public class GamePage
{
    String id;                                      // The ID of the GamePage
    GameBook book;                                  // Reference to the GameBook that created this GamePage
    GameStack<GameElement> elements;                // beinhaltet nur die "Parentlosten" elemente. Verweise zu den anderen Elementen der Page laufen über diese
    boolean visible = false;                        // true: the GamePage and its elements will be visible, false: The GamePage will be invisible
    int zIndex = 0;                                 // z-index of the Page. Pages with a lower z-index will be drawn first (below other pages)

    // cache-variables to prevent memory-allocations
    GameElement currentElment;                      // used to add new elements
    GameElement tempElement;                        // used to update the elements
    GameStack<GameElement> temp;                    // used by everything but the _Draw function to iterate through the elements
    public GameStack<GameElement> renderElements;   // used by the _Draw function to iterate through the elements

    public GameStack<GameElement> getElements()
    {
        return elements;
    }

    public GamePage(String a_id, GameBook a_gamebook)
    {
        this._Init(a_id, a_gamebook, false);
    }

    public GamePage(String a_id, GameBook a_gamebook, boolean a_visible)
    {
        this._Init(a_id, a_gamebook, a_visible);
    }

    // Initializes the default values of the GamePage
    public void _Init(String a_id, GameBook a_gamebook, boolean a_visible)
    {
        this.id = a_id;
        this.visible = a_visible;
        this.book = a_gamebook;
        this.elements = new GameStack<GameElement>();
    }

    // updates the game-mechanics of the elements of this GamePage
    public void _Update(long a_timeDelta, double a_timeFactor)
    {
        if (this.elements == null)
            return;

        this.temp = this.elements;
        while (this.temp != null && this.temp.content != null)
        {
            this.temp.content.Update(a_timeDelta, a_timeFactor);
            this.temp = this.temp.next;
        }
    }

    // draws the elements of this GamePage to the current framebuffer
    public void _Draw(float[] a_mvpMatrix)
    {
        if (this.elements == null)
            return;

        this.renderElements = elements;
        while (this.renderElements.content != null)
        {
            GLES20.glStencilMask(0xFF);
            GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);
            this.renderElements.content.Draw(a_mvpMatrix, 0);
            this.renderElements = this.renderElements.next;
        }
    }

    // gets called once OpenGL is ready to be used
    public void _OGLReady()
    {
        if (this.elements == null)
            return;

        this.renderElements = elements;
        while (this.renderElements.content != null)
        {
            this.renderElements.content.OGLReady();
            this.renderElements = this.renderElements.next;
        }
    }

    // gets called when the screen changes (e.g. on orientiation change, and on startup)
    public void _UpdateScreenDimensions(float a_horzVertexRatio, float a_vertVertexRatio)
    {
        if (this.elements == null)
            return;

        this.renderElements = elements;
        while (this.renderElements.content != null)
        {
            this.renderElements.content.UpdateScreenDimensions(a_horzVertexRatio, a_vertVertexRatio);
            this.renderElements = this.renderElements.next;
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
        this.temp = this.elements;
        while (this.temp != null && this.temp.content != null)
        {
            if (this.temp.content.id.equals(a_id))
                return this.temp.content;

            this.tempElement = this.temp.content.GetElement(a_id);
            if (this.tempElement != null && this.tempElement != null)
                return this.tempElement;

            this.temp = this.temp.next;
        }

        return null;
    }

    /*
        Function: RemoveAllElements
            Removes all elemente from this GamePage
    */
    public void RemoveAllElements()
    {
        this.temp = this.elements;
        while (this.temp != null && this.temp.content != null)
        {
            this._RemoveElement(this.temp);
        }
    }

    /*
        Function: RemoveElement
            Removes an elemente from this GamePage

        Parameter:
            a_id        - String    | ID of the GameElement to remove
    */
    public void RemoveElement(String a_id)
    {
        this.tempElement = this.GetElement(a_id);
        if (this.tempElement == null)
            return;

        if (this.tempElement.parent != null)
            this.tempElement.parent.RemoveChild(a_id);
        else
        {
            this.temp = this.elements;
            while (this.temp != null && this.temp.content != null)
            {
                if (this.temp.content.id.equals(a_id))
                {
                    this._RemoveElement(this.temp);
                    return;
                }

                this.temp = this.temp.next;
            }
        }
    }

    private void _RemoveElement(GameStack<GameElement> a_element)
    {
        a_element.content.RemoveAllChildren();
        a_element.pop();
    }

    /*
        Function: AddLabel
            Adds a new label to the GamePage

        Parameter:
            a_id        - String    | The ID of the new element
            a_parentID  - String    | The ID of the GameElement that should act as the parent for the new element
            a_x         - int       | x-position of the new element
            a_y         - int       | y-position of the new element
            a_text      - String    | The text that the new label will show

        Returns:
            GameElement -> - The new label
    */
    public GameElement AddLabel(String a_id, String a_parentID, int a_x, int a_y, GameFont a_font, String a_text)
    {
        this._CheckElementAlreadyExists(a_id);
        this.tempElement = this._GetParentElement(a_parentID);
        this.currentElment = new Label(a_id, this, this.book, this.tempElement, a_font, a_text);
        return this._AddElement(this.currentElment, this.tempElement, a_x, a_y);
    }

    /*
        Function: AddSheet
            Adds a new label to the GamePage

        Parameter:
            a_id        - String    | The ID of the new element
            a_parentID  - String    | The ID of the GameElement that should act as the parent for the new element
            a_x         - int       | x-position of the new element
            a_y         - int       | y-position of the new element
            a_width     - int       | width of the new element
            a_height    - int       | height of the new element

        Returns:
            GameElement -> - The new Sheet
    */
    public GameElement AddSheet(String a_id, String a_parentID, int a_x, int a_y, int a_width, int a_height)
    {
        this._CheckElementAlreadyExists(a_id);
        this.tempElement = this._GetParentElement(a_parentID);
        this.currentElment = new Sheet(a_id, this, this.book, this.tempElement);
        return this._AddElement(this.currentElment, this.tempElement, a_x, a_y, a_width, a_height);
    }

    // Checks if an element with a given ID already exists
    private void _CheckElementAlreadyExists(String a_id)
    {
        if (this.GetElement(a_id) != null)
            throw new Error("Element '" + a_id + "' already exists on page '" + this.id + "'");
    }

    // Gets the parent-element with a given ID
    private GameElement _GetParentElement(String a_parentID)
    {
        if (a_parentID != null)
        {
            this.tempElement = GetElement(a_parentID);
            if (this.tempElement == null)
                throw new Error("Element can't be added to page '" + this.id + "' with parent '" + a_parentID + "': The parent does not exist on this page");

            return this.tempElement;
        }

        return null;
    }

    // Adds a GameElement to the GamePage
    private GameElement _AddElement(GameElement a_element, GameElement a_parent, int a_x, int a_y, int a_width, int a_height)
    {
        // TODO: don't add the Element on top of the stack, but in a position based on its z-index
        a_element.SetPosition(a_x, a_y);
        a_element.SetSize(a_width, a_height);

        if (a_parent == null)
            this.elements.push(a_element);
        else
            a_parent.AddChild(a_element);

        return a_element;
    }

    // Adds a GameElement to the GamePage
    private GameElement _AddElement(GameElement a_element, GameElement a_parent, int a_x, int a_y)
    {
        return this._AddElement(a_element, a_parent, a_x, a_y, 0, 0);
    }
}
