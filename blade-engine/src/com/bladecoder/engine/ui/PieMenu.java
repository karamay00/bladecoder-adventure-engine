/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.bladecoder.engine.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bladecoder.engine.i18n.I18N;
import com.bladecoder.engine.model.BaseActor;
import com.bladecoder.engine.util.DPIUtils;
import com.bladecoder.engine.util.RectangleRenderer;

public class PieMenu extends com.badlogic.gdx.scenes.scene2d.Group {

	private BitmapFont font;

	private ImageButton lookatButton;
	private ImageButton talktoButton;
	private ImageButton pickupButton;

	private float x = 0, y = 0;

	private BaseActor baseActor = null;

	private final SceneScreen sceneScreen;
	
	private int viewportWidth, viewportHeight;

	public PieMenu(SceneScreen scr) {
		sceneScreen = scr;
		font = scr.getUI().getSkin().getFont("desc");
				
		lookatButton = new ImageButton(scr.getUI().getSkin(), "pie_lookat");
		addActor(lookatButton);
		lookatButton.addListener(new ChangeListener() {			
			@Override
			public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
				if (baseActor != null) {
					sceneScreen.runVerb(baseActor, "lookat", null);
				}

				hide();
			}
		});
		
		talktoButton = new ImageButton(scr.getUI().getSkin(), "pie_talkto");
		addActor(talktoButton);
		talktoButton.addListener(new ChangeListener() {			
			@Override
			public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
				if (baseActor != null) {
					sceneScreen.runVerb(baseActor, "talkto", null);
				}

				hide();
			}
		});
		
		pickupButton = new ImageButton(scr.getUI().getSkin(), "pie_pickup");
		addActor(pickupButton);
		pickupButton.addListener(new ChangeListener() {			
			@Override
			public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
				if (baseActor != null) {
					sceneScreen.runVerb(baseActor, "pickup", null);
				}

				hide();
			}
		});
		
//		addListener(new InputListener() {
//			@Override
//			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
//				hide();
//				
//				return false;
//			}
//		});
	}

	@Override
	public void draw(Batch batch, float alpha) {

		super.draw(batch, alpha);

		// DRAW TARGET DESCRIPTION
		String desc = baseActor.getDesc();

		if (desc != null) {

			if (desc.charAt(0) == '@')
				desc = I18N.getString(desc.substring(1));
					
			TextBounds b = font.getBounds(desc);
			float margin = DPIUtils.UI_SPACE;

			float textX = x - b.width / 2;
			float textY = y - b.height - DPIUtils.UI_SPACE;

			if (textX < 0)
				textX = 0;

			RectangleRenderer.draw(batch, textX - margin, textY - b.height - margin,
					b.width + margin*2, b.height + margin*2, Color.BLACK);
			font.draw(batch, desc, textX, textY);
		}
	}

	public void hide() {
		setVisible(false);
		baseActor = null;
	}

	public void show(BaseActor a, float x, float y) {
		setVisible(true);
		this.x = x;
		this.y = y;
		baseActor = a;
		Actor rightButton;

		if (a.getVerb("talkto") != null) {
			talktoButton.setVisible(true);
			pickupButton.setVisible(false);
			rightButton = talktoButton;
		} else {
			talktoButton.setVisible(false);
			pickupButton.setVisible(true);
			rightButton = pickupButton;
		}
		
		float margin = DPIUtils.getMarginSize();
		
		// FITS TO SCREEN
		if(x < lookatButton.getWidth() + margin)
			this.x = lookatButton.getWidth() + margin;
		else if(x > viewportWidth - lookatButton.getWidth() - margin)
			this.x = viewportWidth - lookatButton.getWidth() - margin;
		
		if(y < margin)
			this.y = margin;
		else if(y > viewportHeight - lookatButton.getHeight() - margin)
			this.y = viewportHeight - lookatButton.getHeight() - margin;
		
		//lookatButton.setPosition(this.x - lookatButton.getWidth() - margin / 2, this.y + margin);
		lookatButton.setPosition(this.x - lookatButton.getWidth() / 2, this.y - lookatButton.getHeight() / 2);
		lookatButton.addAction(Actions
				.moveTo(this.x - lookatButton.getWidth() - margin / 2, this.y + margin, .1f));
		

//		rightButton.setPosition(this.x + margin / 2, this.y + margin);
		rightButton.setPosition(this.x - lookatButton.getWidth() / 2, this.y - lookatButton.getHeight() / 2);
		rightButton.addAction(Actions
				.moveTo(this.x + margin / 2, this.y + margin, .1f));

	}

	public void resize(int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
		
		setBounds(0, 0, width, height);

		float size = DPIUtils.getPrefButtonSize();
		float iconSize = Math.max(size/2, DPIUtils.ICON_SIZE);
		talktoButton.setSize(size, size);
		talktoButton.getImageCell().maxSize(iconSize, iconSize);

		pickupButton.setSize(size, size);
		pickupButton.getImageCell().maxSize(iconSize, iconSize);
		
		lookatButton.setSize(size, size);
		lookatButton.getImageCell().maxSize(iconSize, iconSize);
	}
}
