/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ivli.roim;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Cursor;


import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenu;        

import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
/**
 *
 * @author likhachev
 */        
class Controller implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, WindowChangeListener {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    
    static final int MOUSE_ACTION_NONE   =  00;
    static final int MOUSE_ACTION_SELECT =  01;
    static final int MOUSE_ACTION_ZOOM   =  02;
    static final int MOUSE_ACTION_PAN    =  03;
    static final int MOUSE_ACTION_WINDOW =  04;
    static final int MOUSE_ACTION_LIST   =  05;
    static final int MOUSE_ACTION_WHEEL  =  15;
    static final int MOUSE_ACTION_TOOL   = 100;
    static final int MOUSE_ACTION_MENU   = 200;
    static final int MOUSE_ACTION_ROI    = 500;

    
    protected int iLeftAction   = MOUSE_ACTION_PAN;
    protected int iMiddleAction = MOUSE_ACTION_ZOOM;
    protected int iRightAction  = MOUSE_ACTION_WINDOW;

    class RootActionItem extends ActionItem {
        RootActionItem(int aX, int aY){super(aX, aY);}
        protected  void DoAction(int aX, int aY){} 
        protected  boolean DoWheel(int aX) {
            iControlled.zoom(-aX/Settings.ZOOM_SENSITIVITY_FACTOR, 0, 0); 
            return true;
        }

        protected  boolean DoRelease(int aX, int aY) {return false;}
        protected  void DoPaint(Graphics2D aGC) {}   
    }
    
    ActionItem NewAction(int aType, int aX, int aY) {
        switch (aType){   
            case MOUSE_ACTION_WINDOW: 
                 return new RootActionItem( aX, aY) {
                     public void DoAction(int aX, int aY) {
                        iControlled.setWindow(new Window(iControlled.getWindow().getLevel() + aX - iX, iControlled.getWindow().getWidth() + iY - aY));
                        iControlled.repaint();
                 }}; 
            case MOUSE_ACTION_ZOOM: return new RootActionItem(aX, aY) {public void DoAction(int aX, int aY){
                                                           iControlled.zoom((aX-iX)/Settings.ZOOM_SENSITIVITY_FACTOR, 0, 0);
                }};  
            case MOUSE_ACTION_PAN: 
                return new RootActionItem(aX, aY) {public void DoAction(int aX, int aY){
                                                           iControlled.pan(aX-iX, aY-iY);
                }};  
            case MOUSE_ACTION_WHEEL: return new RootActionItem(aX, aY);
            case MOUSE_ACTION_ROI: 
            case MOUSE_ACTION_MENU:
            case MOUSE_ACTION_NONE:
            default: return new RootActionItem(aX, aY);      
        }        
    }  
    
    private final JMedImagePane iControlled;
    
    private ActionItem iButton;
    //private ActionItem iWheel;  
    private ROI        iSelected;
    

    public Controller(JMedImagePane aC) {
        iControlled = aC;
        //iWheel = NewAction(MOUSE_ACTION_WHEEL, 0, 0);
        register();
    }

    private final void register() {
        iControlled.addMouseListener(this);
        iControlled.addMouseMotionListener(this);
        iControlled.addMouseWheelListener(this);
        iControlled.addKeyListener(this);
    }

    public void mouseEntered(MouseEvent e) {
        iControlled.requestFocusInWindow(); //gain focus
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent e) {    
        if (null != iButton)
            iButton.wheel(e.getWheelRotation());
        else
            iControlled.zoom(e.getWheelRotation()/Settings.ZOOM_SENSITIVITY_FACTOR, 0,0);
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            if (null != (iSelected = iControlled.findRoi(e.getPoint()))) 
                showPopupMenu_Roi(e.getX(), e.getY());
            else 
                showPopupMenu_Context(e.getX(), e.getY());
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (null != iButton) 
            iButton.action(e.getX(), e.getY());
    }

    public void mousePressed(MouseEvent e) {
        if (null != iButton) {
            iButton.action(e.getX(), e.getY());
         //   return;
        }
        else if (null != iSelected) {  // move ROI
            //iControlled.deleteRoi(iSelected);
            iButton = new RootActionItem(e.getX(), e.getY()) {
                protected void DoAction(int aX, int aY) {
                    
                    iControlled.moveRoi(iSelected, aX-iX, aY-iY);
                    iControlled.repaint();//old.createIntersection(iSelected.iShape.getBounds2D())); 
                }    
              protected boolean DoRelease(int aX, int aY) {
                   // iControlled.addRoi(iSelected);
                    iSelected = null;
                    iControlled.repaint();
                    return false;
                  }  
            };
        } 
        else if (SwingUtilities.isLeftMouseButton(e)) {
            iButton = NewAction(iLeftAction, e.getX(), e.getY());
        }
        else if (SwingUtilities.isMiddleMouseButton(e)) {
            iButton = NewAction(iMiddleAction, e.getX(), e.getY());
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            //iRight.Activate(e.getX(), e.getY());
            iButton = NewAction(iRightAction, e.getX(), e.getY());
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (null != iButton && !iButton.release(e.getX(), e.getY())) iButton = null;               
    }

    public void mouseMoved(MouseEvent e) {   
        
        ROI r = iControlled.findRoi(e.getPoint());
                        
        if (null != r ) { // TODO: cleave in two
            iSelected = r;
            if (r.isMovable())            
            iControlled.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            // iSelected = tmp;
        } else {
            iSelected = null;
            iControlled.setCursor(Cursor.getDefaultCursor());
        }            
    }

    public void keyPressed(KeyEvent e) {
    //  System.out.print("\n\t keyPressed");
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {

        }
        else if (e.getKeyCode() == KeyEvent.VK_ALT) {

        }
    }

    public void keyReleased(KeyEvent e) {
       // System.out.print("\n\t keyReleased");
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT: break; 
            case KeyEvent.VK_ALT: break;
            case KeyEvent.VK_R: break;
            case KeyEvent.VK_1: break;
            case KeyEvent.VK_2: break;
            case KeyEvent.VK_3: break;
            case KeyEvent.VK_4: break;
            case KeyEvent.VK_5: break;
            case KeyEvent.VK_6: break;
            case KeyEvent.VK_7: break;
            default: break;
        }
    }

    public void keyTyped(KeyEvent e) {
     // System.out.print("\n\t keyTyped");    
    }

    private static final String KCommandRoiRect = "COMMAND_ROI_CREATE_RECT"; //$NON-NLS-1$ 
    private static final String KCommandRoiOval = "COMMAND_ROI_CREATE_OVAL"; //$NON-NLS-1$
    private static final String KCommandRoiFree = "COMMAND_ROI_CREATE_FREE"; //$NON-NLS-1$
    private static final String KCommandRoiCreate = "COMMAND_ROI_OPERATIONS_CREATE"; //$NON-NLS-1$
    private static final String KCommandRoiDelete = "COMMAND_ROI_OPERATIONS_DELETE"; //$NON-NLS-1$
    private static final String KCommandRoiMove   = "COMMAND_ROI_OPERATIONS_MOVE"; //$NON-NLS-1$
    private static final String KCommandRoiClone  = "COMMAND_ROI_OPERATIONS_CLONE"; //$NON-NLS-1$
    

    public void actionPerformed(ActionEvent e) {
        logger.info(e.getActionCommand() + " ," + e.paramString());
        switch (e.getActionCommand()) {
            case KCommandRoiRect: 
                iButton = new RootActionItem(-1, -1) {                           
                    Path2D iPath = new Path2D.Double();
                    int first = 0;
                    public void DoAction(int aX, int aY) {
                        if (0 == first)
                            iPath.moveTo(aX, aY);
                        else
                            iPath.lineTo(aX, aY);
                        ++first; 
                        iControlled.repaint();//iPath.getBounds()); 
                    }

                    public boolean DoRelease(int aX, int aY) {
                        if (first < 4)
                            return true;
                        else {
                            iPath.closePath();
                            iControlled.addRoi(new ROI(iPath, null));
                            iControlled.repaint();
                        }
                        return false;
                    }
                    public void DoPaint(Graphics2D gc) {
                        gc.draw(iPath);
                    }
                }; break;
            case KCommandRoiOval:
                iButton = new RootActionItem(-1, -1) {

                    Point.Double iTL = new Point.Double(.0, .0);
                    Point.Double iWH = new Point.Double(.0, .0);

                    public void DoAction(int aX, int aY) {
                        ///iTmp.pushPoint();
                        //iTmp.pushPoint((aX  - iOrigin.x)/ iZoom.getScaleX(), (aY - iOrigin.y) / iZoom.getScaleY());
                        if (null == iTL) {
                         ///   iTL = new Point.Double(((aX - iOrigin.x) / iZoom.getScaleX()), ((aY - iOrigin.y) / iZoom.getScaleY()));
                           // iWH = new Point.Double(iTL.x + DEFAULT_ELLIPSE_WIDTH, iTL.y + DEFAULT_ELLIPSE_HEIGHT);
                        } else {
                       ///     iWH = new Point.Double(((aX - iOrigin.x) / iZoom.getScaleX()), ((aY - iOrigin.y) / iZoom.getScaleY()));    
                        }
                        //else iTmp.pushEllips(iTL.x, iTL.y, iWH.x, iWH.y);

                        iControlled.repaint(); ///TODO: optimize it using bounding rectangle 
                    }

                    public ActionItem release() {
                        if (null == iTL || null == iWH)
                            return this;
                        else {
                            iControlled.addRoi(new ROI(new Ellipse2D.Double(iTL.x, iTL.y, iWH.x - iTL.x, iWH.y - iTL.y), null));
                            iControlled.repaint();
                        }
                        return null;
                    }
                    public void DoPaint(Graphics2D gc) {
                       gc.draw(new Ellipse2D.Double(iTL.x, iTL.y, iWH.x - iTL.x, iWH.y - iTL.y));                                
                    }
                }; break;
            case KCommandRoiFree: break;
            case KCommandRoiMove: break;
            case KCommandRoiDelete: 
                iControlled.deleteRoi(iSelected); 
                iSelected = null; 
                iControlled.repaint(); break;
            
            case KCommandRoiClone:   
                iControlled.cloneRoi(iSelected);
                iControlled.repaint();
                iSelected = null;
                break;
            default: break;
        }
    }

    public void windowChanged(WindowChangeEvent anE) {
        if (anE.getSource() != this) { // eliminate possible deadloop
            iControlled.setWindow(anE.getWindow());
            ///iControlled.setWindowWidth(anE.getWindow().getWidth());
        }
    }

    void showPopupMenu_Context(int aX, int aY) {
        final JPopupMenu mnu = new JPopupMenu("ROI"); 
        JMenuItem mi1 = new JMenuItem("Roi ADD");

        mi1.setActionCommand("Cut");
        mi1.addActionListener(this);
        mnu.add(mi1);
        JMenu m1 = new JMenu("ADD");

        JMenuItem mi11 = new JMenuItem("Rectangular");
        mi11.addActionListener(this);
        mi11.setActionCommand(KCommandRoiRect);

        JMenuItem mi12 = new JMenuItem("Oval");
        mi12.addActionListener(this);
        mi12.setActionCommand(KCommandRoiOval); 

        JMenuItem mi13 = new JMenuItem("Free");
        mi13.addActionListener(this);
        mi13.setActionCommand(KCommandRoiFree);

        m1.add(mi11);
        m1.add(mi12);
        m1.add(mi13);

        mnu.add(m1);
        mnu.show(iControlled, aX, aY);
    }
    
    void showPopupMenu_Roi(int aX, int aY) {
        final JPopupMenu mnu = new JPopupMenu("ROI"); 
        
        JMenuItem mi11 = new JMenuItem("Delete");
        mi11.addActionListener(this);
        mi11.setActionCommand(KCommandRoiDelete);

        JMenuItem mi12 = new JMenuItem("Move");
        mi12.addActionListener(this);
        mi12.setActionCommand(KCommandRoiMove); 

        JMenuItem mi13 = new JMenuItem("Clone");
        mi13.addActionListener(this);
        mi13.setActionCommand(KCommandRoiClone);

        mnu.add(mi11);
        mnu.add(mi12);
        mnu.add(mi13);

        mnu.show(iControlled, aX, aY);
    }

    public void paint(Graphics gc) {
        if (null != iButton) iButton.paint(gc);
       /// if (null != iWheel) iWheel.paint(gc);
       /// if (null != iSelected) 
    }


} ///class Controller

