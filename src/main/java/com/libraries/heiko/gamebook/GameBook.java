package com.libraries.heiko.gamebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import com.libraries.heiko.gamebook.tools.GameStack;
import com.libraries.heiko.gamebook.tools.ResourceManager;

/**
 * Created by heiko on 19.02.2016.
 */
public class GameBook extends GLSurfaceView
{
    private GameStack<GamePage> pages;                          // Stack of the currently set GamePages
    public ResourceManager resources;                           // Manages resources like images and audio

    private GameThread gameThread;                              // The thread that triggers the game-mechanics-updates
    public GameRenderer gameRenderer;                           // The OpenGL-Renderer that draws all the things
    public long lastGameFPS = 0;                                // The framerate the gameThread achieved in the last Frame
    public long lastDrawFPS = 0;                                // The framerate the drawThread achieved in the last Frame

    public int screenWidth = 0;                                 // The actual width of the screen
    public int screenHeight = 0;                                // The actual height of the screen
    public int gameWidth = 0;                                   // The gameWidth. Gets scaled up/down to the screen
    public int gameHeight = 0;                                  // The gameHeight. Gets scaled up/down to the screen

    // cache-variables to prevent memory-allocations
    private GameStack<GamePage> drawPages;                      // used by the drawThread to iterate through the GamePages
    private GameStack<GamePage> temp;                           // used by the everything but the drawThread to iterate through the GamePages
    private GameStack<GamePage> temp2;                           // used by the everything but the drawThread to iterate through the GamePages

    // Framework-interal settings
    public Bitmap.Config bitmapConfig = Bitmap.Config.RGB_565;  // The bitmap config to use throughout the game

     public GameBook(Context a_context)
    {
        super(a_context);
        this._Init(a_context, a_context.getResources().getDisplayMetrics().widthPixels, a_context.getResources().getDisplayMetrics().heightPixels);
    }

    public GameBook(Context a_context, int a_gameWidth, int a_gameHeight)
    {
        super(a_context);
        this._Init(a_context, a_gameWidth, a_gameHeight);
    }

    private void _Init(Context a_context, int a_gameWidth, int a_gameHeight)
    {
        this.screenWidth = a_context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = a_context.getResources().getDisplayMetrics().heightPixels;
        this.gameWidth = a_gameWidth;
        this.gameHeight = a_gameHeight;

        pages = new GameStack<GamePage>();
        // TODO: Handle Instance changes (like orientation change).
        // TODO: At the moment, an orientationchange rebuilds the whole view and eats memory

        // initiate the resource-stacks
        this.resources = new ResourceManager(this);

        // Set OpenGL ES Version 2 and initiate the renderer
        this.gameRenderer = new GameRenderer(this, 60);
        setEGLContextClientVersion(2);

        // Set the OpenGL-stencil size to 8 bit
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        this.setRenderer(this.gameRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Initiate the GameThread and start it
        this.gameThread = new GameThread(this, 70);
        this.gameThread.setRunning(true);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        this.gameRenderer.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.gameRenderer.onResume();
    }

    /*
        Function: AddPage
            Adds a new Page to the gamebook

        Parameter:
            a_id    - String    | ID of the new Page

        Returns:
            GamePage -> - The new GamePage
    */
    public GamePage AddPage(String a_id)
    {
        if (this.GetPage(a_id) != null)
            throw new RuntimeException("Page " + a_id + " already exists");

        this.pages.push(new GamePage(a_id, this));
        return this.pages.peek();
    }

    /*
        Function: AddPage
            Adds a new Page to the gamebook

        Parameter:
            a_id        - String    | ID of the new Page
            a_visible   - Boolean   | true: the GamePage will be visible, false: The GamePage will be invisible

        Returns:
            GamePage -> - The new GamePage
    */
    public GamePage AddPage(String a_id, boolean a_visible)
    {
        if (this.GetPage(a_id) != null)
            throw new Error("Page " + a_id + " already exists");

        this.pages.push(new GamePage(a_id, this, a_visible));
        return this.pages.peek();
    }

    /*
        Function: RemovePage
            Removes a Page from the gamebook

        Parameter:
            a_id        - String    | ID of the Page to remove
    */
    public void RemovePage(String a_id)
    {
        this.temp = this.pages;
        while (this.temp.content != null)
        {
            if (this.temp.content.id.equals(a_id))
            {
                this.temp.content.RemoveAllElements();
                this.temp.pop();
                return;
            }

            this.temp = this.temp.next;
        }
    }

    /*
        Function: RemoveAllPages
            Removes all pages from the GameBook
    */
    public void RemoveAllPages()
    {
        this.temp = this.pages;
        while (this.temp.content != null)
        {
            this.temp.content.RemoveAllElements();
            this.temp.pop();
        }
    }

    /*
        Function: GetPage
            Returns the GamePage with a given ID

        Parameter:
            a_id        - String    | ID of the GamePage

        Returns:
            GamePage -> - The GamePage with the given ID
    */
    public GamePage GetPage(String a_id)
    {
        this.temp = this.pages;
        while (this.temp.content != null)
        {
            if (this.temp.content.id.equals(a_id))
                return this.temp.content;

            this.temp = this.temp.next;
        }
        return null;
    }

    public void SetPageDrawOrder(String a_id, int a_drawOrder)
    {
        this.temp = this.pages;
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
            this.temp = this.pages;

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

    // updates the Game-mechanics. Is called by the gameThread
    public void _Update(long a_timeDelta, double a_timeFactor)
    {
        this.temp = this.pages;
        while (this.temp.content != null)
        {
            this.temp.content._Update(a_timeDelta, a_timeFactor);
            this.temp = this.temp.next;
        }
    }

    // Draws teh current game-status to the next free Framebuffer. Is called by the drawThread
    public void _Draw(float[] a_mvpMatrix)
    {
        this.drawPages = pages;
        while (this.drawPages.content != null)
        {
            if (this.drawPages.content.visible == true)
                this.drawPages.content._Draw(a_mvpMatrix);

            this.drawPages = this.drawPages.next;
        }
    }

    // gets called once OpenGL is ready to be used
    public void _OGLReady()
    {
        this.resources._OGLReady();
        this.drawPages = pages;
        while (this.drawPages.content != null)
        {
            this.drawPages.content._OGLReady();
            this.drawPages = this.drawPages.next;
        }
    }

    // gets called when the screen changes (e.g. on orientiation change, and on startup)
    public void _UpdateScreenDimensions(float a_horzVertexRatio, float a_vertVertexRatio)
    {
        this.resources._UpdateScreenDimensions(a_horzVertexRatio, a_vertVertexRatio);
        this.drawPages = pages;
        while (this.drawPages.content != null)
        {
            this.drawPages.content._UpdateScreenDimensions(a_horzVertexRatio, a_vertVertexRatio);
            this.drawPages = this.drawPages.next;
        }
    }

    // The GameThread triggers the updateing of the game-mechanics
    private class GameThread extends Thread
    {
        private double frameTime = 0;           // The time every frame should use (in nanoseconds)
        private long lastFrameDuration = 0;     // The time the last frame actually used (in nanoseconds)
        private double lastRenderBudget = 0;    // The time the last thread slept to achieve its frametime (in nanoseconds)
        private GameBook gamebook;              // Reference to the GameBook-Instance to call the _Update function
        private boolean running = false;        // true: The thread is running, false: The thread is not running

        public GameThread(GameBook a_gamebook, int a_targetFramerate)
        {
            super();
            this.gamebook = a_gamebook;
            this.frameTime = (1000000000/a_targetFramerate);
        }

        @Override
        public void run()
        {
            long startTime;
            while (this.running)
            {
                startTime = System.nanoTime();
                this.gamebook._Update(this.lastFrameDuration, this.lastFrameDuration / this.frameTime);
                this.lastRenderBudget = this.frameTime - (System.nanoTime() - startTime);

                synchronized (this)
                {
                    try
                    {
                        if (this.lastRenderBudget > 0)
                            this.wait((long) this.lastRenderBudget / 1000000, (int) (this.lastRenderBudget - (int) (this.lastRenderBudget / 1000000) * 1000000)/8);
                    } catch (Exception a_exception)
                    {
                        a_exception.printStackTrace();
                    }
                }

                this.lastFrameDuration = (System.nanoTime() - startTime);
                this.gamebook.lastGameFPS = 1000000000/this.lastFrameDuration;
            }
        }

        // starts/stops the thread
        public void setRunning(boolean a_running)
        {
            if (this.running == a_running)
                return;

            this.running = a_running;
            if (this.running == true && (this.isInterrupted() || this.getState() == Thread.State.NEW))
                this.start();
            else if (this.running == false && !this.isInterrupted())
                this.interrupt();
        }
    }
}
