package com.libraries.heiko.gamebook.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.libraries.heiko.gamebook.GameBook;


/**
 * Created by heiko on 28.02.2016.
 */
public class ResourceManager
{
    private GameStack<GameResource> images;     // holds resources of the type 'image'
    private GameStack<GameResource> tempStack;  // used to iterate trough resurce-stacks
    private GameResource tempResource;          // used to temporarily hold a new resource when adding it
    private GameBook book;                      // reference to the GameBook

    public ResourceManager(GameBook a_book)
    {
        this.book = a_book;
        this.images = new GameStack<GameResource>();
        this.tempStack = new GameStack<GameResource>();
    }

    /*
        Function: GetImage
            Gets a previously stored image

        Parameter:
            a_id    - String    | ID of the stored image

        Returns:
            Bitmap -> - The requestes image
    */
    public Bitmap GetImage(String a_id)
    {
        return (Bitmap) this._GetResource(a_id, this.images);
    }

    /*
        Function: AddImage
            Stores an image using an id to later retrieve it

        Parameter:
            a_id    - String    | ID of the stored image
            a_image - Bitmap    | Image to store

        Returns:
            Bitmap -> - The image that just got stored
    */
    public Bitmap AddImage(String a_id, Bitmap a_image)
    {
        return (Bitmap) this._AddResource(a_id, this.images, a_image);
    }

    /*
        Function: AddImage
            Stores an image using an id to later retrieve it

        Parameter:
            a_id    - String    | ID of the stored image
            a_image - Resource  | Resource of the image to store

        Returns:
            Bitmap -> - The image that just got stored
    */
    public Bitmap AddImage(String a_id, int a_image)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = this.book.bitmapConfig;
        return (Bitmap) this._AddResource(a_id, this.images, BitmapFactory.decodeResource(this.book.getContext().getResources(), a_image, options));
    }

    /*
        Function: AddImage
            Stores an image using an id to later retrieve it

        Parameter:
            a_id    - String    | ID of the stored image
            a_path  - String    | Path to the image to store

        Returns:
            Bitmap -> - The image that just got stored
    */
    public Bitmap AddImage(String a_id, String a_path)
    {
        return (Bitmap) this._AddResource(a_id, this.images, BitmapFactory.decodeFile(a_path));
    }

    /*
        Function: RemoveImage
            Removes a previously stored image from the ResourceManager

        Parameter:
            a_id    - String    | ID of the image to remove
    */
    public void RemoveImage(String a_id)
    {
        this._RemoveResource(a_id, this.images);
    }

    private Object _GetResource(String a_id, GameStack<GameResource> a_targetStack)
    {
        this.tempStack = a_targetStack;
        while (this.tempStack.content != null)
        {
            if (this.tempStack.content.id.equals(a_id))
                return this.tempStack.content.resource;

            this.tempStack = this.tempStack.next;
        }
        return null;
    }

    private Object _AddResource(String a_id, GameStack<GameResource> a_targetStack, Object a_resource)
    {
        if (this._GetResource(a_id, a_targetStack) != null)
            throw new RuntimeException("resource already registered: " + a_id);

        this.tempResource = new GameResource(a_id, a_resource);
        a_targetStack.push(this.tempResource);
        this.tempResource = null;
        return a_targetStack.peek().resource;
    }

    private void _RemoveResource(String a_id, GameStack<GameResource> a_targetStack)
    {
        this.tempStack = a_targetStack;
        while (this.tempStack.content != null)
        {
            if (this.tempStack.content.id.equals(a_id))
            {
                this.tempStack.pop();
                return;
            }

            this.tempStack = this.tempStack.next;
        }
    }
}
