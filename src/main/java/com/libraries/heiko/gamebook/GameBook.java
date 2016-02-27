package com.libraries.heiko.gamebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.libraries.heiko.gamebook.tools.GameStack;

/**
 * Created by heiko on 19.02.2016.
 */
public class GameBook extends GLSurfaceView implements SurfaceHolder.Callback
{
    GameStack<GamePage> pages;              // Stack of the currently set GamePages

    GameThread gameThread;                  // The thread that triggers the game-mechanics-updates
    DrawThread drawThread;                  // The thread that triggers the framebuffer-updates
    RenderThread renderThread;              // The thread that renders the current framebuffer to the screen
    long lastGameFPS = 0;                   // The framerate the gameThread achieved in the last Frame
    long lastDrawFPS = 0;                   // The framerate the drawThread achieved in the last Frame
    long lastRenderFPS = 0;                 // The framerate the renderThread achieved in the last Frame

    int screenWidth = 0;                    // The actual width of the screen
    int screenHeight = 0;                   // The actual height of the screen
    int gameWidth = 720;                    // The width the game is working with
    int gameHeight = 1280;                  // The height the game is working with

    GameStack<Bitmap> framebuffer;          // Stack of avaliable framebuffers to prevent half-frames to be drawn
    GameStack<Bitmap> lastCompletedBuffer;  // Reference zo the last fully drawn Frame
    Canvas backbuffer;                      // The canvas used to draw the next frame to the framebuffer

    // cache-variables to prevent memory-allocations
    GameStack<GamePage> drawPages;          // used by the drawThread to iterate through the GamePages
    GameStack<GamePage> temp;               // used by the everything but the drawThread to iterate through the GamePages

    public GameBook(Context a_context)
    {
        super(a_context);
        this.screenWidth = a_context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = a_context.getResources().getDisplayMetrics().heightPixels;
        pages = new GameStack<GamePage>();

        // add the callback to the surfaceholder to react to events
        this.getHolder().addCallback(this);

        // Initiate the framebuffers
        this.framebuffer = new GameStack<Bitmap>();
        for (int i = 0; i < 4; i++)
        {
            this.framebuffer.push(Bitmap.createBitmap(this.gameWidth, this.gameHeight, Bitmap.Config.RGB_565));
        }
        this.lastCompletedBuffer = this.framebuffer;
        this.backbuffer = new Canvas();

        // Initiate the threads
        this.gameThread = new GameThread(this, 100);
        this.drawThread = new DrawThread(this, 80);
        this.renderThread = new RenderThread(getHolder(), this, 64);

    }

    // SurfaceHolder-Callbacks
    @Override
    public void surfaceChanged(SurfaceHolder a_holder, int a_format, int a_width, int a_height)
    {

    }

    @Override
    public void surfaceCreated(SurfaceHolder a_holder)
    {
        // Surface is created, so start the game loop
        this.gameThread.setRunning(true);
        this.drawThread.setRunning(true);
        this.renderThread.setRunning(true);
        System.gc();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder a_holder)
    {
        boolean retry = true;
        while(retry)
        {
            try
            {
                this.gameThread.setRunning(false);
                this.gameThread.join();
                this.drawThread.setRunning(false);
                this.drawThread.join();
                this.renderThread.setRunning(false);
                this.renderThread.join();
            }
            catch(InterruptedException a_exception)
            {
                a_exception.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent a_event)
    {
        return super.onTouchEvent(a_event);
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
            throw new RuntimeException("Page " + a_id + " already exists");

        this.pages.push(new GamePage(a_id, this, a_visible));
        return this.pages.peek();
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
            if (this.temp.content.id == a_id)
                return this.temp.content;

            this.temp = this.temp.next;
        }
        return null;
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
    public void _Draw(Canvas a_targetCanvas)
    {
        a_targetCanvas.drawColor(Color.BLACK);
        this.drawPages = pages;
        while (this.drawPages.content != null)
        {
            if (this.drawPages.content.visible == true)
                this.drawPages.content._Draw(a_targetCanvas);

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

    // The DrawThread triggers the updateing of the current Framebuffer
    private class DrawThread extends Thread
    {
        private double frameTime = 0;           // The time every frame should use (in nanoseconds)
        private double lastRenderBudget = 0;    // The time the last thread slept to achieve its frametime (in nanoseconds)
        private GameBook gamebook;              // Reference to the GameBook-Instance to call the _Draw function
        private boolean running;                // true: The thread is running, false: The thread is not running

        public DrawThread(GameBook a_gamebook, int a_targetFramerate)
        {
            super();
            this.gamebook = a_gamebook;
            this.frameTime = (1000000000/a_targetFramerate);
        }

        @Override
        public void run()
        {
            long startTime;
            GameStack<Bitmap> nextBuffer = this.gamebook.lastCompletedBuffer;
            while (this.running)
            {
                startTime = System.nanoTime();
                if (this.gamebook.lastCompletedBuffer.next.content == null)
                    nextBuffer = this.gamebook.framebuffer;
                else
                    nextBuffer = this.gamebook.lastCompletedBuffer.next;

                this.gamebook.backbuffer.setBitmap(nextBuffer.content);
                this.gamebook._Draw(this.gamebook.backbuffer);
                this.gamebook.lastCompletedBuffer = nextBuffer;

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

                this.gamebook.lastDrawFPS = 1000000000/(System.nanoTime() - startTime);
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

    // The RenderThread renders the current Framebuffer to the screen
    private class RenderThread extends Thread
    {
        private double frameTime = 0;           // The time every frame should use (in nanoseconds)
        private double lastRenderBudget = 0;    // The time the last thread slept to achieve its frametime (in nanoseconds)
        private SurfaceHolder surfaceHolder;    // The Surfaceholder which is used to update the screen
        private GameBook gamebook;              // Reference to the GameBook-Instance to call the _Update function
        private boolean running;                // true: The thread is running, false: The thread is not running
        private Canvas renderCanvas;

        public RenderThread(SurfaceHolder a_surfaceHolder, GameBook a_gamebook, int a_targetFramerate)
        {
            super();
            this.surfaceHolder = a_surfaceHolder;
            this.gamebook = a_gamebook;
            this.frameTime = (1000000000/a_targetFramerate);
        }

        @Override
        public void run()
        {
            long startTime;
            Rect dstRect = new Rect();
            while (this.running)
            {
                startTime = System.nanoTime();
                try
                {
                        this.renderCanvas = this.surfaceHolder.lockCanvas();
                        this.renderCanvas.getClipBounds(dstRect);
                        this.renderCanvas.drawBitmap(this.gamebook.lastCompletedBuffer.content, null, dstRect, null);
                } catch (Exception a_exception)
                {
                    a_exception.printStackTrace();
                } finally
                {
                    if (this.renderCanvas != null)
                        this.surfaceHolder.unlockCanvasAndPost(this.renderCanvas);
                }

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

                this.gamebook.lastRenderFPS = 1000000000/(System.nanoTime() - startTime);
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
