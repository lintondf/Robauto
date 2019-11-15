package com.bluelightning;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

public class Overlay extends JComponent {
	
	static float TRANSPARENCY = 0.4f;

    public Overlay() {
    }
    
    public void paintComponent(Graphics g) {
    	/// wheels: 50 & 256
    	int xc = this.getBounds().x + this.getBounds().width/2;
    	int yc = this.getBounds().y + this.getBounds().height/2;
    	int top = this.getBounds().y + 1*this.getBounds().height/10;
    	int bot = this.getBounds().y + 9*this.getBounds().height/10;
    	float h = (float) bot - (float) top;
    	float w = 100.0f / 430.0f * h;
    	float sx = 1.0f;
    	float sy = 1.0f;
    	
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform at = AffineTransform.getTranslateInstance(xc, yc);
        at.scale(sx, sy);
//        g2.setTransform(at);
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, 
            RenderingHints.VALUE_ANTIALIAS_ON);
        AlphaComposite alcom = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, TRANSPARENCY );
        g2.setComposite(alcom);
        float h1 = (430.0f - 50.0f) / 430.0f * h;
        Shape s = new RoundRectangle2D.Float(((float) xc) - 0.5f*w, (float) top, w, h1, 0.05f*w, 0.05f*w);
        float ho = 0.03f * h;
        float h2 = h - ho - h1;
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(20));
        g2.draw(s);
        s = new RoundRectangle2D.Float(((float) xc) - 0.5f*w, (float) top + h1 + ho, w, h2, 0.01f*w, 0.01f*w);
        g2.draw(s);
    }

}
