package com.libraries.heiko.gamebook;

import android.opengl.GLES20;

import com.libraries.heiko.gamebook.controls.Label;
import com.libraries.heiko.gamebook.controls.MapLayer;
import com.libraries.heiko.gamebook.controls.Sheet;
import com.libraries.heiko.gamebook.tools.GameFont;
import com.libraries.heiko.gamebook.tools.GameStack;

/**
 * Created by heiko on 19.02.2016.
 */
public class GamePage
{
    public String id;                                   // The ID of the GamePage
    private GameBook book;                              // Reference to the GameBook that created this GamePage
    private GameStack<GameElement> elements;            // beinhaltet nur die "Parentlosten" elemente. Verweise zu den anderen Elementen der Page laufen über diese
    boolean visible = false;                            // true: the GamePage and its elements will be visible, false: The GamePage will be invisible
    int drawOrder = 0;                                  // z-index of the Page. Pages with a lower z-index will be drawn first (below other pages)

    // cache-variables to prevent memory-allocations
    private GameElement currentElment;                  // used to add new elements
    private GameElement tempElement;                    // used to update the elements
    private GameStack<GameElement> temp;                // used by everything but the _Draw function to iterate through the elements
    private GameStack<GameElement> temp2;               // used by everything but the _Draw function to iterate through the elements
    public GameStack<GameElement> renderElements;       // used by the _Draw function to iterate through the elements

    public GameStack<GameElement> getElements()
    {
        return elements;
    }

    public GamePage(String a_id, GameBook a_gamebook)
    {
        this.Init(a_id, a_gamebook, false);
    }

    public GamePage(String a_id, GameBook a_gamebook, boolean a_visible)
    {
        this.Init(a_id, a_gamebook, a_visible);
    }

    // Initializes the default values of the GamePage
    private void Init(String a_id, GameBook a_gamebook, boolean a_visible)
    {
        this.id = a_id;
        this.visible = a_visible;
        this.book = a_gamebook;
        this.elements = new GameStack<GameElement>();
    }

    // updates the game-mechanics of the elements of this GamePage
    void Update(long a_timeDelta, double a_timeFactor, long a_timePassed)
    {
        if (this.elements == null)
            return;

        this.temp = this.elements;
        while (this.temp != null && this.temp.content != null)
        {
            if (this.temp.content.needsUpdate == true)
                this.temp.content.Update(a_timeDelta, a_timeFactor, a_timePassed);

            this.temp = this.temp.next;
        }
    }

    // draws the elements of this GamePage to the current framebuffer
    void Draw(float[] a_mvpMatrix)
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
    void OGLReady()
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
    void UpdateScreenDimensions(float a_horzVertexRatio, float a_vertVertexRatio)
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
            this.RemoveElement(this.temp);
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
                    this.RemoveElement(this.temp);
                    return;
                }

                this.temp = this.temp.next;
            }
        }
    }

    private void RemoveElement(GameStack<GameElement> a_element)
    {
        a_element.content.RemoveAllChildren();
        a_element.pop();
    }

    /*
        Function: SetChildDrawOrder
            Sets the drawOrder-Index of this page. Higher draworder = later rendering = above other elements

        Parameter:
            a_drawOrder - Integer   | The drawOrder-index the page should get
    */
    public void SetDrawOrder(int a_drawOrder)
    {
        if (this.drawOrder == a_drawOrder)
            return;

        this.book.SetPageDrawOrder(this.id, a_drawOrder);
    }

    /*
        Function: SetChildDrawOrder
            Sets the drawOrder-Index of a child-element of this page. Higher draworder = later rendering = above other elements

        Parameter:
            a_drawOrder - Integer   | The drawOrder-index the page should get
    */
    public void SetChildDrawOrder(String a_id, int a_drawOrder)
    {
        this.temp = this.elements;
        while (this.temp.content != null)
        {
            if (this.temp.content.id.equals(a_id))
                break;

            this.temp = this.temp.next;
        }

        // if the element with the given ID was not found, abort
        if (this.temp.content == null)
            return;

        this.temp2 = this.temp;
        if (this.temp.content.drawOrder > a_drawOrder)
            this.temp = this.elements;

        // find the first entry that has a higher zIndex than the element should get
        while (this.temp.content != null)
        {
            if (this.temp.content.drawOrder > a_drawOrder)
                break;

            this.temp = this.temp.next;
        }

        // remove the Element from its old position and push it to the new one
        this.temp.push(this.temp2.pop());
        this.temp.content.drawOrder = a_drawOrder;
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
        this.CheckElementAlreadyExists(a_id);
        this.tempElement = this.GetParentElement(a_parentID);
        this.currentElment = new Label(a_id, this, this.book, this.tempElement, a_font, a_text);
        return this.AddElement(this.currentElment, this.tempElement, a_x, a_y);
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
        this.CheckElementAlreadyExists(a_id);
        this.tempElement = this.GetParentElement(a_parentID);
        this.currentElment = new Sheet(a_id, this, this.book, this.tempElement);
        return this.AddElement(this.currentElment, this.tempElement, a_x, a_y, a_width, a_height);
    }

    /*
        Function: AddMapLayer
            Adds a new AddMapLayer to the GamePage

        Parameter:
            a_id            - String    | The ID of the new element
            a_parentID      - String    | The ID of the GameElement that should act as the parent for the new element
            a_x             - int       | x-position of the new element
            a_y             - int       | y-position of the new element
            a_width         - int       | width of the new element
            a_height        - int       | height of the new element
            a_mapWidth      - int       | width of the new mapLayer in tiles
            a_mapHeight     - int       | height of the new mapLayer in tiles
            a_tileWidth     - int       | width mapTiles in pixel
            a_tileHeight    - int       | height of the mapTiles in pixel

        Returns:
            GameElement -> - The new MapLayer
    */
    public GameElement AddMapLayer(String a_id, String a_parentID, int a_x, int a_y, int a_mapWidth, int a_mapHeight, int a_tileWidth, int a_tileHeight)
    {
        this.CheckElementAlreadyExists(a_id);
        this.tempElement = this.GetParentElement(a_parentID);
        this.currentElment = new MapLayer(a_id, this, this.book, this.tempElement, a_mapWidth, a_mapHeight, a_tileWidth, a_tileHeight);
        return this.AddElement(this.currentElment, this.tempElement, a_x, a_y, a_mapWidth * a_tileHeight, a_mapHeight * a_tileHeight);
    }

    // Checks if an element with a given ID already exists
    private void CheckElementAlreadyExists(String a_id)
    {
        if (this.GetElement(a_id) != null)
            throw new Error("Element '" + a_id + "' already exists on page '" + this.id + "'");
    }

    // Gets the parent-element with a given ID
    private GameElement GetParentElement(String a_parentID)
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
    private GameElement AddElement(GameElement a_element, GameElement a_parent, int a_x, int a_y, int a_width, int a_height)
    {
        a_element.SetPosition(a_x, a_y);
        a_element.SetSize(a_width, a_height);

        if (a_parent == null)
            this.elements.push(a_element);
        else
            a_parent.AddChild(a_element);

        return a_element;
    }

    // Adds a GameElement to the GamePage
    private GameElement AddElement(GameElement a_element, GameElement a_parent, int a_x, int a_y)
    {
        return this.AddElement(a_element, a_parent, a_x, a_y, 0, 0);
    }
}
