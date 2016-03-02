package com.libraries.heiko.gamebook.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.libraries.heiko.gamebook.GameBook;


/**
 * Created by heiko on 28.02.2016.
 */
public class ResourceManager
{
    private GameStack<GameResource> images;
    private GameStack<GameResource> tempStack;
    private GameResource tempResource;
    private GameBook book;

    public ResourceManager(GameBook a_book)
    {
        this.book = a_book;
        this.images = new GameStack<GameResource>();
        this.tempStack = new GameStack<GameResource>();
    }

    public Bitmap GetImage(String a_id)
    {
        return (Bitmap) this.GetResource(a_id, this.images);
    }

    public Bitmap AddImage(String a_id, Bitmap a_image)
    {
        return (Bitmap) this.AddResource(a_id, this.images, a_image);
    }

    public Bitmap AddImage(String a_id, int a_image)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inScaled = false;
        options.inPreferredConfig = this.book.bitmapConfig;
        return (Bitmap) this.AddResource(a_id, this.images, BitmapFactory.decodeResource(this.book.getContext().getResources(), a_image, options));
    }

    public Bitmap AddImage(String a_id, String a_path)
    {
        return (Bitmap) this.AddResource(a_id, this.images, BitmapFactory.decodeFile(a_path));
    }

    public void RemoveImage(String a_id)
    {
        this.RemoveResource(a_id, this.images);
    }

    private Object GetResource(String a_id, GameStack<GameResource> a_targetStack)
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

    private Object AddResource(String a_id, GameStack<GameResource> a_targetStack, Object a_resource)
    {
        if (this.GetResource(a_id, a_targetStack) != null)
            throw new RuntimeException("resource already registered: " + a_id);

        this.tempResource = new GameResource(a_id, a_resource);
        a_targetStack.push(this.tempResource);
        this.tempResource = null;
        return a_targetStack.peek().resource;
    }

    private void RemoveResource(String a_id, GameStack<GameResource> a_targetStack)
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
