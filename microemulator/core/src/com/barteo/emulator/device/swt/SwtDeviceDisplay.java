/*
 *  MicroEmulator
 *  Copyright (C) 2002 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.barteo.emulator.device.swt;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

import com.barteo.emulator.EmulatorContext;
import com.barteo.emulator.MIDletAccess;
import com.barteo.emulator.MIDletBridge;
import com.barteo.emulator.app.ui.swt.ImageFilter;
import com.barteo.emulator.app.ui.swt.SwtDeviceComponent;
import com.barteo.emulator.app.ui.swt.SwtGraphics;
import com.barteo.emulator.device.Device;
import com.barteo.emulator.device.DeviceFactory;
import com.barteo.emulator.device.InputMethod;
import com.barteo.emulator.device.MutableImage;
import com.barteo.emulator.device.impl.Button;
import com.barteo.emulator.device.impl.DeviceDisplayImpl;
import com.barteo.emulator.device.impl.PositionedImage;
import com.barteo.emulator.device.impl.Rectangle;
import com.barteo.emulator.device.impl.SoftButton;

public class SwtDeviceDisplay implements DeviceDisplayImpl 
{
    EmulatorContext context;

	Rectangle displayRectangle;
	Rectangle displayPaintable;

	boolean isColor;
	int numColors;

	Color backgroundColor;
	Color foregroundColor;

	PositionedImage upImage;
	PositionedImage downImage;

	PositionedImage mode123Image;
	PositionedImage modeAbcUpperImage;
	PositionedImage modeAbcLowerImage;

	boolean scrollUp = false;
	boolean scrollDown = false;


	public SwtDeviceDisplay(EmulatorContext context) 
	{
		this.context = context;
	}


	public MutableImage getDisplayImage()
	{
		return context.getDisplayComponent().getDisplayImage();
	}


	public int getHeight() 
	{
		return displayPaintable.height;
	}

	public int getWidth() 
	{
		return displayPaintable.width;
	}


	public int getFullHeight() 
	{
		return displayRectangle.height;
	}


	public int getFullWidth() 
	{
		return displayRectangle.width;
	}


	public boolean isColor() 
	{
		return isColor;
	}


	public int numColors() 
	{
		return numColors;
	}


	public void paint(SwtGraphics g) 
	{
		Device device = DeviceFactory.getDevice();
		
		g.setBackground(g.getColor(new RGB(
				backgroundColor.getRed(), 
				backgroundColor.getGreen(), 
				backgroundColor.getBlue())));
		g.fillRectangle(0, 0, displayRectangle.width, displayPaintable.y);
		g.fillRectangle(0, displayPaintable.y,
				displayPaintable.x, displayPaintable.height);
		g.fillRectangle(displayPaintable.x + displayPaintable.width, displayPaintable.y,
				displayRectangle.width - displayPaintable.x - displayPaintable.width, displayPaintable.height);
		g.fillRectangle(0, displayPaintable.y + displayPaintable.height,
				displayRectangle.width, displayRectangle.height - displayPaintable.y - displayPaintable.height);

		g.setForeground(g.getColor(new RGB(
				foregroundColor.getRed(), 
				foregroundColor.getGreen(), 
				foregroundColor.getBlue())));
		for (Enumeration s = device.getSoftButtons().elements(); s.hasMoreElements();) {
			((SwtSoftButton) s.nextElement()).paint(g);
		}

		int inputMode = device.getInputMethod().getInputMode();
		if (inputMode == InputMethod.INPUT_123) {
			g.drawImage(((SwtImmutableImage) mode123Image.getImage()).getImage(), 
			        mode123Image.getRectangle().x, mode123Image.getRectangle().y);
		} else if (inputMode == InputMethod.INPUT_ABC_UPPER) {
			g.drawImage(((SwtImmutableImage) modeAbcUpperImage.getImage()).getImage(), 
			        modeAbcUpperImage.getRectangle().x, modeAbcUpperImage.getRectangle().y);
		} else if (inputMode == InputMethod.INPUT_ABC_LOWER) {
			g.drawImage(((SwtImmutableImage) modeAbcLowerImage.getImage()).getImage(), 
			        modeAbcLowerImage.getRectangle().x, modeAbcLowerImage.getRectangle().y);
		}

		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma != null) {
			Displayable current = ma.getDisplayAccess().getCurrent();
			org.eclipse.swt.graphics.Rectangle oldclip = g.getClipping();
			if (!(current instanceof Canvas) 
					|| ((Canvas) current).getWidth() != displayRectangle.width
					|| ((Canvas) current).getHeight() != displayRectangle.height) {
				g.setClipping(new org.eclipse.swt.graphics.Rectangle(displayPaintable.x, displayPaintable.y, displayPaintable.width, displayPaintable.height));
				g.translate(displayPaintable.x, displayPaintable.y);
			}
			Font f = g.getFont();

			ma.getDisplayAccess().paint(new SwtDisplayGraphics(g, getDisplayImage()));

			g.setFont(f);
			
			if (!(current instanceof Canvas) 
					|| ((Canvas) current).getWidth() != displayRectangle.width
					|| ((Canvas) current).getHeight() != displayRectangle.height) {
				g.translate(-displayPaintable.x, -displayPaintable.y);
				g.setClipping(oldclip);
			}
		}

		if (scrollUp) {
			g.drawImage(((SwtImmutableImage) upImage.getImage()).getImage(), 
			        upImage.getRectangle().x, upImage.getRectangle().y);
		}
		if (scrollDown) {
			g.drawImage(((SwtImmutableImage) downImage.getImage()).getImage(), 
			        downImage.getRectangle().x, downImage.getRectangle().y);
		}
	}


	public void repaint() 
	{
		context.getDisplayComponent().repaint();
	}


	public void setScrollDown(boolean state) 
	{
		scrollDown = state;
	}


	public void setScrollUp(boolean state) 
	{
		scrollUp = state;
	}


	public Rectangle getDisplayRectangle() 
	{
		return displayRectangle;
	}


	public Color getBackgroundColor() 
	{
		return backgroundColor;
	}


	public Color getForegroundColor() 
	{
		return foregroundColor;
	}
	
	
	public Image createImage(int width, int height) 
	{
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException();
		}

		return new SwtMutableImage(width, height);
	}

	
	public Image createImage(String name) 
			throws IOException 
	{
		return getImage(name);
	}

	
	public Image createImage(javax.microedition.lcdui.Image source) 
	{
		if (source.isMutable()) {
			return new SwtImmutableImage((SwtMutableImage) source);
		} else {
			return source;
		}
	}

	
	public Image createImage(byte[] imageData, int imageOffset, int imageLength) 
	{
		ByteArrayInputStream is = new ByteArrayInputStream(imageData, imageOffset, imageLength);
		try {
			return getImage(is);
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex.toString());
		}
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setNumColors(int)
	 */
    public void setNumColors(int i)
    {
        numColors = i;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setIsColor(boolean)
     */
    public void setIsColor(boolean b)
    {
        isColor = b;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setBackgroundColor(java.awt.Color)
     */
    public void setBackgroundColor(Color color)
    {
        backgroundColor = color;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setForegroundColor(java.awt.Color)
     */
    public void setForegroundColor(Color color)
    {
        foregroundColor = color;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setDisplayRectangle(java.awt.Rectangle)
     */
    public void setDisplayRectangle(Rectangle rectangle)
    {
        displayRectangle = rectangle;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setDisplayPaintable(java.awt.Rectangle)
     */
    public void setDisplayPaintable(Rectangle rectangle)
    {
        displayPaintable = rectangle;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setUpImage(com.barteo.emulator.device.impl.PositionedImage)
     */
    public void setUpImage(PositionedImage object)
    {
        upImage = object;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setDownImage(com.barteo.emulator.device.impl.PositionedImage)
     */
    public void setDownImage(PositionedImage object)
    {
        downImage = object;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setMode123Image(com.barteo.emulator.device.impl.PositionedImage)
     */
    public void setMode123Image(PositionedImage object)
    {
        mode123Image = object;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setModeAbcLowerImage(com.barteo.emulator.device.impl.PositionedImage)
     */
    public void setModeAbcLowerImage(PositionedImage object)
    {
        modeAbcLowerImage = object;
    }


    /* (non-Javadoc)
     * @see com.barteo.emulator.device.impl.DeviceDisplayImpl#setModeAbcUpperImage(com.barteo.emulator.device.impl.PositionedImage)
     */
    public void setModeAbcUpperImage(PositionedImage object)
    {
        modeAbcUpperImage = object;
    }

    
    public Image createSystemImage(String str) 
    		throws IOException 
	{
		InputStream is;

		is = context.getClassLoader().getResourceAsStream(str);
		if (is == null) {
			throw new IOException();
		}

		return new SwtImmutableImage(SwtDeviceComponent.createImage(is));
	}

    
	private Image getImage(String str) 
			throws IOException 
	{
		InputStream is = context.getClassLoader().getResourceAsStream(str);

		if (is == null) {
			throw new IOException(str + " could not be found.");
		}

		return getImage(is);
	}

	
	private Image getImage(InputStream is) 
			throws IOException 
	{
		ImageFilter filter = null;
		if (isColor()) {
			filter = new RGBImageFilter();
		} else {
			if (numColors() == 2) {
				filter = new BWImageFilter();
			} else {
				filter = new GrayImageFilter();
			}
		}

		return new SwtImmutableImage(SwtDeviceComponent.createImage(is, filter));
	}


    public Button createButton(String name, Rectangle rectangle, String keyName, char[] chars)
    {
        return new SwtButton(name, rectangle, keyName, chars);
    }


    public SoftButton createSoftButton(String name, Rectangle rectangle, String keyName, Rectangle paintable, String alignmentName, Vector commands)
    {
        return new SwtSoftButton(name, rectangle, keyName, paintable, alignmentName, commands);
    }

}