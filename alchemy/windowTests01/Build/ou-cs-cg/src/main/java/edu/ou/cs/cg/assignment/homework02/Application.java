//******************************************************************************
// Copyright (C) 2016-2022 University of Oklahoma Board of Trustees.
//******************************************************************************
// Last modified: Fri Feb 18 6:38pm R Travis Pierce
//******************************************************************************
// Major Modification History:
//
// 20220223 [pierce]:   Original file.
//
//******************************************************************************
// Notes:
//
// Warning! This code uses deprecated features of OpenGL, including immediate
// mode vertex attribute specification, for sake of easier classroom learning.
// See www.khronos.org/opengl/wiki/Legacy_OpenGL
//
//******************************************************************************

package edu.ou.cs.cg.assignment.homework02;

//import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

//******************************************************************************

/**
 * The <CODE>Application</CODE> class.<P>
 *
 * @author  Chris Weaver, R Travis Pierce
 * @version %I%, %G%
 */
public final class Application
	implements GLEventListener, Runnable
{
	//**********************************************************************
	// Private Class Members
	//**********************************************************************

	private static final String		    DEFAULT_NAME = "Windows Test";
	private static final Dimension		DEFAULT_SIZE = new Dimension(1280, 720);

	//**********************************************************************
	// Public Class Members
	//**********************************************************************

	public static final GLUT			MYGLUT = new GLUT();
	public static final Random			RANDOM = new Random();

	//**********************************************************************
	// Private Members
	//**********************************************************************

	// State (internal) Variables
	private int				w;				    // Canvas width
	private int				h;				    // Canvas height
	private int				k = 0;			    // Animation counter
    private int spiralCounter = 0;
    private double angle = 45;
    private int lightningCounter = 0;
    // Menu Window Variables
    int numMenuWindows = 5;                     // Number of Windows in Menu
    int menuWindowRadius = 64;                  // squareWidth/2
    int menuWindowBuffer = 32;                  // Space between item menu windows
    
    // Counters
    // Items
    // Bubbler
    double kCenterBubbler = 0;                  // start at center y
    double kCenterBubblerMax = 8;               // max offset from center
    boolean kCenterBubblerUp = true;            // are we moving up or down?
    double centerBubblerRadius = 40;            // radius of the center bubbler
    // Bubbles
    double kBubblesMax = 148;                    //
    double kBubbles[] = {0, kBubblesMax/4, kBubblesMax/2, 3*kBubblesMax/4, kBubblesMax};  // Starting offsets
    
    // Oozer
    double kLeftOozers[] = {0, 13, 24, 35, 86, 37, 22, 48, 31, 53, 63, 58};     // start at bottom of circle
    double kLeftOozerRadii[] = {6.4, 5.3, 4.8, 6.7, 8, 5.4, 7.2, 8, 6.5, 6.6, 8, 7.7};
    double kLeftOozerMax = 60;               // max offset from center
    double kLeftMiniOozers[] = {25, 43, 34, 25, 66, 37, 26, 48, 39, 53, 83, 58};  // start at bottom of circle
    double kLeftMiniOozerRadii[] = {2.4, 3.3, 3.8, 2.7, 4, 1.4, 2.2, 4, 3.5, 2.6, 4, 3.7};
    
    // HypnoCircle
    boolean white = false;                                                  // start black
    double circleRadius = 240;
    boolean circleUp = false;
    
    // Question Mark
    double kQuestionMark = 0;                   // start at center y
    double kQuestionMarkMax = 8;                // max offset from center
    boolean kQuestionMarkUp = true;             // are we moving up or down?
    
    
	private TextRenderer    renderer;

	private float			thickline;		// Line thickness
	private boolean			fillpolys;		// Fill polygons?
    

	//**********************************************************************
	// Main
	//**********************************************************************

	public static void	main(String[] args)
	{
		SwingUtilities.invokeLater(new Application(args));
	}

	//**********************************************************************
	// Constructors and Finalizer
	//**********************************************************************

	public Application(String[] args)
	{
	}

	//**********************************************************************
	// Override Methods (Runnable)
	//**********************************************************************

	public void	run()
	{
		GLProfile		profile = GLProfile.getDefault();
		GLCapabilities	capabilities = new GLCapabilities(profile);
		GLCanvas		canvas = new GLCanvas(capabilities);	// Single-buffer
		//GLJPanel		canvas = new GLJPanel(capabilities);	// Double-buffer
		JFrame			frame = new JFrame(DEFAULT_NAME);

		// Rectify display scaling issues when in Hi-DPI mode on macOS.
		edu.ou.cs.cg.utilities.Utilities.setIdentityPixelScale(canvas);

		// Specify the starting width and height of the canvas itself
		canvas.setPreferredSize(DEFAULT_SIZE);

		// Populate and show the frame
		frame.setBounds(50, 50, 200, 200);
		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Exit when the user clicks the frame's close button
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});

		// Register this class to update whenever OpenGL needs it
		canvas.addGLEventListener(this);

		// Have OpenGL call display() to update the canvas 60 times per second
		FPSAnimator	animator = new FPSAnimator(canvas, 60);

		animator.start();
	}

	//**********************************************************************
	// Override Methods (GLEventListener)
	//**********************************************************************

	// Called immediately after the GLContext of the GLCanvas is initialized.
	public void	init(GLAutoDrawable drawable)
	{
		w = drawable.getSurfaceWidth();
		h = drawable.getSurfaceHeight();

		renderer = new TextRenderer(new Font("Serif", Font.PLAIN, 124),
									true, true);

		initPipeline(drawable);
	}

	// Notification to release resources for the GLContext.
	public void	dispose(GLAutoDrawable drawable)
	{
		renderer = null;
	}

	// Called to initiate rendering of each frame into the GLCanvas.
	public void	display(GLAutoDrawable drawable)
	{
		update(drawable);
		render(drawable);
	}

	// Called during the first repaint after a resize of the GLCanvas.
	public void	reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		this.w = w;
		this.h = h;
	}

	//**********************************************************************
	// Private Methods (Rendering)
	//**********************************************************************

	// Update the scene model for the current animation frame.
	private void	update(GLAutoDrawable drawable)
	{
        k++;                                        // Advance main animation counter
        
        // ANIMATION COUNTERS HERE $%&$%&$%&$%&$%&$%&$%&$%&$%&$%&


        //Lightning counter
        if (lightningCounter < 7){
            lightningCounter++;
        }
        else {
            lightningCounter = 0;
        }
        //Archimedean Spiral
        if (spiralCounter < 99){
            spiralCounter++;
        }
        else{
            spiralCounter = 0;
        }
        // CenterBubbler
        if (kCenterBubblerUp) {                      // if CenterBubbler moving up
            kCenterBubbler = kCenterBubbler + 0.1;                        // increment kCenterBubbler
        } else {                                    // otherwise
            kCenterBubbler = kCenterBubbler - 0.1;                        // decrement kCenterBubbler
        }
        
        if (kCenterBubbler > kCenterBubblerMax) {     // if CenterBubbler rises past max
            kCenterBubblerUp = false;                // turn it around
        }
        if (kCenterBubbler < -kCenterBubblerMax + 2) {    // if CenterBubbler lowers past min
            kCenterBubblerUp = true;                 // turn it around
        }
        
        // Bubbles
        for (int i = 0; i < kBubbles.length; i++) { // for all kBubbles[]
            kBubbles[i] = kBubbles[i] + 0.8;        // increment kBubbles[i]
            if (kBubbles[i] > kBubblesMax) {            // if we exceed the max
                kBubbles[i] = 0;                        // reset
            }
        }
        
        // LeftOozers
        for (int i = 0; i < kLeftOozers.length; i++) {
            if (kLeftOozers[i] < kLeftOozerMax) {
                kLeftOozers[i] = kLeftOozers[i] + 0.2;
            }
            if (kLeftOozers[i] > kLeftOozerMax) {
                kLeftOozers[i] = 0;
            }
        }
        
        // LeftMiniOozers
        for (int i = 0; i < kLeftMiniOozers.length; i++) {
            if (kLeftMiniOozers[i] < kLeftOozerMax) {
                kLeftMiniOozers[i] = kLeftMiniOozers[i] + 0.6;
            }
            if (kLeftMiniOozers[i] > kLeftOozerMax) {
                kLeftMiniOozers[i] = 0;
            }
        }
        
        
        // Question Mark / Hidden Element Symbol
        if (kQuestionMarkUp) {                      // if Question Mark moving up
            kQuestionMark = kQuestionMark + 0.4;                        // increment kQuestionMark
        } else {                                    // otherwise
            kQuestionMark = kQuestionMark - 0.4;                        // decrement kQuestionMark
        }
        
        if (kQuestionMark > kQuestionMarkMax) {     // if Question Mark rises past max
            kQuestionMarkUp = false;                // turn it around
        }
        if (kQuestionMark < -kQuestionMarkMax) {    // if Question Mark lowers past min
            kQuestionMarkUp = true;                 // turn it around
        }
	}

	// Render the scene model and display the current animation frame.
	private void	render(GLAutoDrawable drawable)
	{
        thickline = 0.5f * ((180 / 15) % 12);       // regular lines are fines.
        fillpolys = true;                           // Just leave it on.
        
		GL2	gl = drawable.getGL().getGL2();

		gl.glClearColor(0.1f, 0.1f, 0.1f, 0.0f);	// Black background
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);         // Clear the buffer

        setProjection(gl);                          // Use screen coordinates
        
        // DRAW HERE &%$&%$&%$&%$&%$&%$&%$&%$&%$&%$&%$&%$
        drawItemWindows(gl);                        // Draw Item Windows
        // Draw Locked Item Symbol
       // drawQuestionMark(drawable, 1280/2 + -2*menuWindowRadius*2.4 - 28, 7.5*(720/9) - 40 + kQuestionMark);
        
        
        // Draw Locked Item Symbol
        //drawQuestionMark(drawable, 1280/2 + 2*menuWindowRadius*2.4 - 28, 7.5*(720/9) - 40 + kQuestionMark);
        drawItems(gl);
        drawFloor(gl, w/2, -h*2.6, h*3);
        drawTable(gl, w/2, 200, 200);
        drawHypnoFadeCircle(gl,  1280/2 + -2*menuWindowRadius*2.4 ,  7.5*(720/9),48);
        drawJar(gl,  1280/2 + 2*menuWindowRadius*2.4, 7.5*(720/9) - 30,35);
        animateSpiral(gl, 1280/2 + -2*menuWindowRadius*2.4, 7.5*(720/9),60);
       
        //drawBubbles(gl);
	}

	//**********************************************************************
	// Private Methods (Pipeline)
	//**********************************************************************

	// www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glBlendFunc.xml
	private void	initPipeline(GLAutoDrawable drawable)
	{
		GL2	gl = drawable.getGL().getGL2();

		// Make points easier to see on Hi-DPI displays
		gl.glEnable(GL2.GL_POINT_SMOOTH);	// Turn on point anti-aliasing
	}

	// Position and orient the default camera to view in 2-D, in pixel coords.
	private void	setProjection(GL2 gl)
	{
		GLU	glu = GLU.createGLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);		// Prepare for matrix xform
		gl.glLoadIdentity();						// Set to identity matrix
		glu.gluOrtho2D(0.0f, 1280.0f, 0.0f, 720.0f);// 2D translate and scale
	}
    
    private void drawFloor(GL2 gl, double cx, double cy, double r)
    {
        setColor(gl, 0, 0, 0);                    // Black
        drawCircle(gl, cx, cy, r);           // center
    }
    
    private void drawTable(GL2 gl, double cx, double cy, double r)
    {
        setColor(gl, 32, 32, 32);                    // Charcoal
        gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(cx + r*1.4, cy - 10);     // top left
            gl.glVertex2d(cx + r*1.4, 0);           // bottom left
            gl.glVertex2d(cx - r*1.4, 0);           // bottom right
            gl.glVertex2d(cx - r*1.4, cy - 10);     // top right
        gl.glEnd();
        setColor(gl, 0, 0, 0);                    // Black
        gl.glBegin(GL2.GL_POLYGON);
        for (double i = 0; i < 360; i++) {
            gl.glVertex2d(r*1.4*(Math.cos(i)) + cx, r*0.6*(Math.sin(i)) + cy - 10);
        }
        gl.glEnd();
        setColor(gl, 255, 255, 255);                    // White
        gl.glBegin(GL2.GL_POLYGON);
        for (double i = 0; i < 360; i++) {
            gl.glVertex2d(r*1.3*(Math.cos(i)) + cx, r*0.5*(Math.sin(i)) + cy);
        }
        gl.glEnd();
    }
    
    private void drawBubbles(GL2 gl, double cx, double cy)
    {
        setColor(gl, 0, 200, 0);                    // Green
        drawCircle(gl, cx + 4*(Math.sin(k%4)), cy + kBubbles[0], 8 + 2*(Math.random()));           // center
        setColor(gl, 0, 255, 0);                    // Green
        drawCircle(gl, cx + 4*(Math.sin(k%4)), cy + kBubbles[0], 8 - 2*(Math.random()));           // center
        setColor(gl, 0, 200, 0);
        drawCircle(gl, cx - 30 + 9*(Math.sin(k%8)), cy + kBubbles[1], 12  + 2*(Math.random()));     // left of center
        setColor(gl, 0, 255, 0);
        drawCircle(gl, cx - 30 + 9*(Math.sin(k%8)), cy + kBubbles[1], 12  - 2*(Math.random()));     // left of center
        setColor(gl, 0, 200, 0);
        drawCircle(gl, cx + 26 + 12*(Math.sin(k%12)), cy + kBubbles[2], 3  + 2*(Math.random()));     // right of center
        setColor(gl, 0, 255, 0);
        drawCircle(gl, cx + 26 + 12*(Math.sin(k%12)), cy + kBubbles[2], 3  - 2*(Math.random()));     // right of center
        setColor(gl, 0, 200, 0);
        drawCircle(gl, cx - 33 + 9*(Math.sin(k%9)), cy + kBubbles[4], 6  + 2*(Math.random()));     // left of center
        setColor(gl, 0, 255, 0);
        drawCircle(gl, cx - 33 + 9*(Math.sin(k%9)), cy + kBubbles[4], 6  - 2*(Math.random()));     // left of center
        setColor(gl, 0, 200, 0);
        drawCircle(gl, cx + 40 + 7*(Math.sin(k%4)), cy + kBubbles[3], 7  + 2*(Math.random()));     // right of center
        setColor(gl, 0, 255, 0);
        drawCircle(gl, cx + 40 + 7*(Math.sin(k%4)), cy + kBubbles[3], 7  - 2*(Math.random()));     // right of center
    }
    
    private void drawOoze(GL2 gl, double cx, double cy, double r)
    {
        /*
        setColor(gl, 60, 80, 255);                    // Bright Greenish Blue
        for (int i = 0; i < kLeftMiniOozers.length; i++) {
            for (int j = 0; j < kLeftMiniOozers[i]; j++) {
                drawCircle(gl, cx  + (r - 8)*(Math.cos(i + 1)), cy + (r - 8)*(Math.sin(i + 1)) - kLeftMiniOozers[i] + j, kLeftMiniOozerRadii[i]);
            }
        }
        */
        setColor(gl, 0, 0, 255);                    // Blue
        for (int i = 0; i < kLeftOozers.length; i++) {
            for (int j = 0; j < kLeftOozers[i]; j++) {
                drawCircle(gl, cx  + (r - 8)*(Math.cos(i)), cy + (r - 8)*(Math.sin(i)) - kLeftOozers[i] + j, kLeftOozerRadii[i]);
            }
        }
    }
    
    private void drawHypnoCircle(GL2 gl, double dx, double dy, boolean colorChange) {
        for (int j = 10; j > 0; j--) {
            gl.glBegin(GL2.GL_POLYGON);
            if (white) {
                setColor(gl, 255, 255, 255);    // White
            } else {
                setColor(gl, 0, 0, 0);          // Black
            }
            for (int i = 0; i < 360; i++) {
                if (white) {
                    setColor(gl, i%255 + 100, 0, 255);
                } else {
                    setColor(gl, i%255 + 100, 255, i%255);
                }
                gl.glVertex2d((2*j)*(Math.cos(i)) + (3*j*(k%Math.random()))*(Math.cos(i/3)) + (dx), (2*j)*(Math.sin(i)) + (3*j*(k%Math.random()))*(Math.sin(i/3)) + (dy));
            }
            gl.glEnd();
            white = !white;                     // toggle
        }
        
        if (colorChange) {
            if (k % 2 == 0) {
                white = !white;
            }
        }
    }
    

    private void drawSpiral(GL2 gl, double cx, double cy, double radius) {
        gl.glLineWidth(2);
        double dt = 1.0/32.0;                         // numSteps = 32
        gl.glBegin(GL2.GL_LINE_STRIP);
        double a = 0.5;
        double b = 10;
        for (double t = 0.0; t <= 4.7; t = t + dt) {
            setColor(gl, 0, 0, 0);
            //gl.glVertex2d((radius*(Math.cos(2*(Math.PI)*t)) + cx), (radius*(Math.sin(2*(Math.PI)*t)) + cy));
            
            gl.glVertex2d(((a+b*(t))*(Math.cos(2*(Math.PI)*t)) + cx),((a+b*(t))*(Math.sin(2*(Math.PI)*t)) + cy));
            
        }
        angle +=5;
        gl.glEnd();
    }

    private void drawJar(GL2 gl, double cx, double cy, double radius){ //Draw jar with x,y starting at bottom
        setColor(gl, 193, 193, 193);                    // White
        fillRect(gl, (int)cx-45, (int)cy-1, 91, 58);
        drawOval(gl, cx, cy, radius, false);//bottom oval
        drawOval(gl, cx, cy+60, radius, true); //top oval, "lid"
        
        drawLightning(gl, cx-36, cy-9);
    }

    private static final Point[] OUTLINE_LIGHTNING = new Point[] {
        //new Point(0,0),
        new Point(31,29),
        new Point(10,27),
        new Point(60,74),
        new Point(36,36),
        new Point(60,36),
        new Point(0,0)
    };

    
    private void drawLightning(GL2 gl, double cx, double cy){
        
        

        setColor(gl, 0, 32, 152);
        fillPoly(gl, (int)cx, (int)cy, OUTLINE_LIGHTNING);

        gl.glBegin(GL2.GL_POLYGON);

		Point2D.Double[]	t = new Point2D.Double[6];
		Color[]			rgb = new Color[3];

        rgb[0] = new Color(0,126,255); //med blue
        rgb[1] =  new Color(0, 32, 152); //dark blue

        //These points represent the lightning outline but for the gradients
        t[0] = new Point2D.Double(cx+32,cy+29);
        t[1] = new Point2D.Double(cx+10,cy+27);
        t[2] = new Point2D.Double(cx+60, cy+74);
        t[3] = new Point2D.Double(cx+34, cy+36);
        t[4] = new Point2D.Double(cx+60, cy+36);
        t[5] = new Point2D.Double(cx, cy);

        float[] rgb1 = rgb[0].getRGBColorComponents(null);
        float[] rgb2 = rgb[1].getRGBColorComponents(null);

        gl.glColor3f(rgb2[0], rgb2[1],rgb2[2]);
        gl.glVertex2d(t[0].x,t[0].y);
        gl.glColor3f(rgb1[0], rgb1[1],rgb1[2]);
        gl.glVertex2d(t[1].x,t[1].y);
        gl.glVertex2d(t[2].x,t[2].y);
        gl.glColor3f(rgb2[0], rgb2[1],rgb2[2]);
        gl.glVertex2d(t[3].x,t[3].y);
        gl.glColor3f(rgb1[0], rgb1[1],rgb1[2]);
        gl.glVertex2d(t[4].x,t[4].y);
        gl.glVertex2d(t[5].x,t[5].y);

        gl.glEnd();
        setColor(gl, 0, 230, 230); //electric blue
        edgePoly(gl, (int)cx, (int)cy, OUTLINE_LIGHTNING);
        animateLightningMovers(gl, cx+20, cy+45, LIGHTNING_MOVERS);
    }

    private void drawLightningMovers(GL2 gl, double cx, double cy){
       // gl.glVertex2d(cx+ ,cy+ );
       setColor(gl, 0, 255, 255);
        gl.glBegin(GL2.GL_LINE_STRIP);

        gl.glVertex2d(cx,cy);
        gl.glVertex2d(cx+3 ,cy+5 );
        gl.glVertex2d(cx+8 ,cy+7 );
        gl.glVertex2d(cx+6 ,cy+10 );
        gl.glVertex2d(cx+12 ,cy+15 );
        gl.glVertex2d(cx+16 ,cy+13);
        gl.glVertex2d(cx+21 ,cy+20 );
        gl.glVertex2d(cx+27 ,cy+18 );
        gl.glVertex2d(cx+30 ,cy+23 );
        gl.glEnd();
    }

    private static final Point[] LIGHTNING_MOVERS = new Point[]{
        new Point(0,0),
        new Point(3,5),
        new Point(8,7),
        new Point(6,10),
        new Point(12,15),
        new Point(16,13),
        new Point(21,20),
        new Point(27,18),
        new Point(30,23),
    };
    private void animateLightningMovers(GL2 gl, double cx, double cy, Point[] outline){
        Point2D.Double[]	t = new Point2D.Double[9];
        t[0] = new Point2D.Double(cx, cy);
        t[1] = new Point2D.Double(cx+3 ,cy+5 );
        t[2] = new Point2D.Double(cx+8 ,cy+7 );
        t[3] = new Point2D.Double(cx+6 ,cy+10 );
        t[4] = new Point2D.Double(cx+12 ,cy+15 );
        t[5] = new Point2D.Double(cx+16 ,cy+13);
        t[6] = new Point2D.Double(cx+21 ,cy+20 );
        t[7] = new Point2D.Double(cx+27 ,cy+18 );
        t[8] = new Point2D.Double(cx+30 ,cy+23 );

        setColor(gl, 0, 255, 255);
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex2d(outline[0].x,outline[0].y);
        gl.glVertex2d(outline[1].x,outline[1].y);
        gl.glEnd();
    }
    private void animateSpiral(GL2 gl, double cx, double cy, double radius){
       
        gl.glTranslated(cx,cy,0);
        gl.glRotated(angle,0,0,-1);
        gl.glTranslated(-cx,-cy,0);
        drawSpiral(gl, cx,  cy, radius);

    }
    private void drawItems(GL2 gl)
    {
        //setColor(gl, 255, 255, 255);                // White
        // CENTER ITEM - GREEN BUBBLER
        setColor(gl, 0, 200, 0);                    // Green
        drawCircle(gl, 1280/2 + 0*menuWindowRadius*2.4, 7.5*(720/9) + kCenterBubbler, 2*(Math.random()) + centerBubblerRadius);     // center
        setColor(gl, 0, 255, 0);                    // Green
        drawCircle(gl, 1280/2 + 0*menuWindowRadius*2.4, 7.5*(720/9) + kCenterBubbler, centerBubblerRadius - 2*(Math.random()));     // center
        setColor(gl, 60, 255, 60);                    // Green
        drawCircle(gl, 1280/2 + 0*menuWindowRadius*2.4, 7.5*(720/9) + kCenterBubbler, centerBubblerRadius - 8 - 4*(Math.random()));     // center
        setColor(gl, 100, 255, 100);                    // Green
        drawCircle(gl, 1280/2 + 0*menuWindowRadius*2.4, 7.5*(720/9) + kCenterBubbler, centerBubblerRadius - 16 - 12*(Math.random()));     // center
        drawBubbles(gl, 1280/2 + 0*menuWindowRadius*2.4, 7.5*(720/9));      // bubbles
        
        // LEFT ITEM - BLUE OOZY
        setColor(gl, 0, 0, 255);                    // Blue
        drawCircle(gl, 1280/2 + -1*menuWindowRadius*2.4, 7.5*(720/9), 48);    // left
        drawOoze(gl, 1280/2 + -1*menuWindowRadius*2.4, 7.5*(720/9), 48);    // ooze
        
        // RIGHT ITEM - RED SUN
        setColor(gl, 255, 0, 255);                    // Red
        double randomX = 8*(Math.random()) - 4;
        double randomY = 8*(Math.random()) - 4;
        drawCircle(gl, 1280/2 + 1*menuWindowRadius*2.4 + randomX, 7.5*(720/9) + randomY, 34);     // right
        drawHypnoCircle(gl, 1280/2 + 1*menuWindowRadius*2.4 + randomX, 7.5*(720/9) + randomY, true);
    }
    
    private void drawOval(GL2 gl, double cx, double cy, double r, boolean lid) {
        setColor(gl, 193, 193, 193);                    // White
        gl.glBegin(GL2.GL_POLYGON);
        for (double i = 0; i < 360; i++) {
            gl.glVertex2d(r*1.3*(Math.cos(i)) + cx, r*0.5*(Math.sin(i)) + cy);
        }
        gl.glEnd();
        if(lid)
        {
            setColor(gl, 0, 0, 0); //black
            gl.glBegin(GL.GL_LINE_LOOP);
            for (double i = 0; i < 360; i++) {
                gl.glVertex2d(r*1.3*(Math.cos(i)) + cx, r*0.33*(Math.sin(i)) + (cy+5));
            }
            gl.glEnd();
        }
    }
    private void drawCircle(GL2 gl, double cx, double cy, double radius)
    {
        double dt = 1.0/32.0;                         // numSteps = 32
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        //gl.glVertex2d(cx, cy);
        for (double t = 0.0; t <= 1.0; t = t + dt) {
            //gl.glVertex2d((radius*(Math.cos(i)) + cx), (radius*(Math.sin(i)) + cy));
            gl.glVertex2d((radius*(Math.cos(2*(Math.PI)*t)) + cx), (radius*(Math.sin(2*(Math.PI)*t)) + cy));
        }
        gl.glEnd();
    }

    private void drawHypnoFadeCircle(GL2 gl, double cx, double cy, double radius)
    {
        double dt = 1.0/32.0;                         // numSteps = 32
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        Color rgb = new Color(0,0,0); //black
        float[]		rgb1 = rgb.getRGBColorComponents(null);

        //gl.glColor3f(1.0f,1.0f,1.0f); //white
        setColor(gl, 255, 255, 255);
        gl.glVertex2d(cx, cy);
        for (double t = 0.0; t <= 1.0; t = t + dt) {
            //gl.glVertex2d((radius*(Math.cos(i)) + cx), (radius*(Math.sin(i)) + cy));
            gl.glColor3f(rgb1[0], rgb1[1], rgb1[2]);
            gl.glVertex2d((radius*(Math.cos(2*(Math.PI)*t)) + cx), (radius*(Math.sin(2*(Math.PI)*t)) + cy));
        }
        gl.glEnd();
    }
    
    private void drawItemWindows(GL2 gl)
    {
        setColor(gl, 255, 255, 255);                // white
        for (int i = -numMenuWindows/2; i < numMenuWindows/2 + 1; i++) {
            for (int j = 0; j < 10; j++) {
                drawSquare(gl, 1280/2 + i*menuWindowRadius*2.4, 7.5*(720/9), menuWindowRadius - j);
            }
        }
    }
    // Draw square from given center
    // need to set color before calling this function
    private void    drawSquare(GL2 gl, double cx, double cy, int r)
    {
        gl.glBegin(GL2.GL_LINE_LOOP);           // start up a line loop
        gl.glVertex2d(cx - r, cy + r);          // top left
        gl.glVertex2d(cx - r, cy - r);          // bottom left
        gl.glVertex2d(cx + r, cy - r);          // bottom right
        gl.glVertex2d(cx + r, cy + r);          // top right
        gl.glEnd();
    }
    
    // Draw the gradient for the ground
    private void    drawGroundGradient(GL2 gl)
    {
        int yMax = 132;
        
        gl.glBegin(GL2.GL_POLYGON);
        setColor(gl, 0, 64, 0);                 // Dark Green
        gl.glVertex2i(0, 0);                    // bottom left
        gl.glVertex2i(w, 0);                    // bottom right
        setColor(gl, 0, 255, 0);                // Green
        gl.glVertex2i(w, yMax);                 // top right
        gl.glVertex2i(0, yMax);                 // top left

        gl.glEnd();
    }
    
	private void	drawText(GLAutoDrawable drawable)
	{
		renderer.beginRendering(w, h);
		renderer.setColor(0.75f, 0.75f, 0.75f, 1.0f);
		renderer.draw("Application", 2, h - 14);
		renderer.endRendering();
	}
    
    private void    drawQuestionMark(GLAutoDrawable drawable, double cx, double cy)
    {
        renderer.beginRendering(w, h);
        renderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        renderer.draw("?", (int)cx, (int)cy);
        renderer.endRendering();
    }

	// Draw a window, given its center.
	private void	drawWindow(GL2 gl, int dx, int dy, boolean shade)
	{
		int	ww = 20;
		int	hh = 20;
        

		setColor(gl, 255, 255, 128);			// Light yellow
		fillRect(gl, dx - ww, dy - hh, 2 * ww, 2 * hh);
        
        // If desired, this window can also have a shade/curtain combo
        if (shade) {
            // LEFT SHADE
            setColor(gl, 64, 64, 64);                       // Charcoal
            gl.glBegin(GL2.GL_POLYGON);
                gl.glVertex2i(dx - ww, dy - 16);            // bottom left
                gl.glVertex2i(dx + ww, dy - 16);            // bottom right
                gl.glVertex2i(dx + ww, dy + hh);            // top right
                gl.glVertex2i(dx - ww, dy + hh);            // top left
            gl.glEnd();
            
            // LEFT CURTAIN
            setColor(gl, 255, 200, 0);                      // Gold
            gl.glBegin(GL2.GL_POLYGON);
                gl.glVertex2i(dx - ww, dy - hh);            // bottom left
                gl.glVertex2i(dx - (ww/2), dy - (hh/2));
                gl.glVertex2i(dx - (ww/4), dy + (hh/2));
                gl.glVertex2i(dx, dy + hh);                 // top right
                gl.glVertex2i(dx - ww, dy + hh);            // top left
            gl.glEnd();
            
            // RIGHT CURTAIN
            setColor(gl, 255, 200, 0);                      // Gold
            gl.glBegin(GL2.GL_POLYGON);
                gl.glVertex2i(dx + (ww/2), dy - (hh/2));
                gl.glVertex2i(dx + (ww/4), dy + (hh/2));
                gl.glVertex2i(dx + ww, dy - hh);            // bottom right
                gl.glVertex2i(dx + ww, dy + hh);            // top right
                gl.glVertex2i(dx, dy + hh);                 // top left
            gl.glEnd();
        }

		setColor(gl, 0, 0, 0);					// Black
		edgeRect(gl, dx - ww, dy - hh, 2 * ww, 2 * hh);
	}

	//**********************************************************************
	// Private Methods (Utility Functions)
	//**********************************************************************

	// Sets color, normalizing r, g, b, a values from max 255 to 1.0.
	private void	setColor(GL2 gl, int r, int g, int b, int a)
	{
		gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
	}

	// Sets fully opaque color, normalizing r, g, b values from max 255 to 1.0.
	private void	setColor(GL2 gl, int r, int g, int b)
	{
		setColor(gl, r, g, b, 255);
	}

	// Fills a rectangle having lower left corner at (x,y) and dimensions (w,h).
	private void	fillRect(GL2 gl, int x, int y, int w, int h)
	{
		if (!fillpolys)
			return;

		gl.glBegin(GL2.GL_POLYGON);

		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);

		gl.glEnd();
	}

	// Edges a rectangle having lower left corner at (x,y) and dimensions (w,h).
	private void	edgeRect(GL2 gl, int x, int y, int w, int h)
	{
		gl.glLineWidth(thickline);

		gl.glBegin(GL.GL_LINE_LOOP);

		gl.glVertex2i(x+0, y+0);
		gl.glVertex2i(x+0, y+h);
		gl.glVertex2i(x+w, y+h);
		gl.glVertex2i(x+w, y+0);

		gl.glEnd();

		gl.glLineWidth(1.0f);
	}

	// Fills a polygon defined by a starting point and a sequence of offsets.
	private void	fillPoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		if (!fillpolys)
			return;

		gl.glBegin(GL2.GL_POLYGON);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();
	}

	// Edges a polygon defined by a starting point and a sequence of offsets.
	private void	edgePoly(GL2 gl, int startx, int starty, Point[] offsets)
	{
		gl.glLineWidth(thickline);

		gl.glBegin(GL2.GL_LINE_LOOP);

		for (int i=0; i<offsets.length; i++)
			gl.glVertex2i(startx + offsets[i].x, starty + offsets[i].y);

		gl.glEnd();

		gl.glLineWidth(1.0f);
	}
}

//******************************************************************************
