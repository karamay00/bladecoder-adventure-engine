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
package com.bladecoder.engineeditor.scneditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.bladecoder.engine.anim.AtlasAnimationDesc;
import com.bladecoder.engine.anim.AnimationDesc;
import com.bladecoder.engine.anim.Tween;
import com.bladecoder.engine.assets.EngineAssetManager;
import com.bladecoder.engine.model.AtlasRenderer;
import com.bladecoder.engine.model.ImageRenderer;
import com.bladecoder.engine.spine.SpineRenderer;
import com.bladecoder.engine.model.Sprite3DRenderer;
import com.bladecoder.engine.model.ActorRenderer;
import com.bladecoder.engine.util.RectangleRenderer;
import com.bladecoder.engineeditor.model.ChapterDocument;
import com.bladecoder.engineeditor.ui.EditAnimationDialog;

public class AnimationWidget extends Widget {
	private String source;
	private AnimationDesc fa;
	private ActorRenderer renderer;
	EditAnimationDialog editFADialog;
	

	public AnimationWidget(EditAnimationDialog createEditFADialog) {
		this.editFADialog = createEditFADialog;
	}

	
	public void setSource(String type, String source) {
		this.source = source;
		
		if(renderer != null) {
			renderer.dispose();
			renderer = null;
		}
		
		
		if(type.equals(ChapterDocument.SPRITE3D_ACTOR_TYPE)) {
			renderer = new Sprite3DRenderer();
			((Sprite3DRenderer)renderer).setSpriteSize(new Vector2( Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		} else if(type.equals(ChapterDocument.SPINE_ACTOR_TYPE)) {
			renderer = new SpineRenderer();
		} else if(type.equals(ChapterDocument.IMAGE_ACTOR_TYPE)) {
			renderer = new ImageRenderer();			
		} else {
			renderer = new AtlasRenderer();
		}
		
		renderer.loadAssets();
		EngineAssetManager.getInstance().finishLoading();
		renderer.retrieveAssets();
	}
	
	public String[] getAnimations() {
		return renderer.getInternalAnimations(source);
	}
	
	public void setAnimation(String id, String speedStr, String typeStr) {
		if (source!=null && id != null && !source.isEmpty() && !id.isEmpty()) {
			
			int type = Tween.REPEAT;
			float speed = 2.0f;
			
			if(!speedStr.isEmpty())
				speed = Float.parseFloat(speedStr);
			
			if(typeStr.equals("yoyo"))
				type = Tween.PINGPONG;
			
			if(renderer instanceof AtlasRenderer)
				fa = new AtlasAnimationDesc();
			else 
				fa = new AnimationDesc();
			
			fa.set(id, source, speed, 0.0f, Tween.INFINITY, type,
					null, null, null, false, true);
			
			renderer.addAnimation(fa);
			
			renderer.startAnimation(fa.id, Tween.FROM_FA, 1, null);
		}
	}
	
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		
		if(renderer == null || renderer.getCurrentAnimation() == null)
			return;
		
		Color tmp = batch.getColor();
		batch.setColor(Color.WHITE);
		
		renderer.update(Gdx.graphics.getDeltaTime());
		
		RectangleRenderer.draw((SpriteBatch)batch, getX(), getY(), getWidth(), getHeight(), Color.MAGENTA);
		
		float scalew =   getWidth() /  renderer.getWidth();
		float scaleh =   getHeight() /  renderer.getHeight();
		float scale = scalew>scaleh?scaleh:scalew;
		renderer.draw((SpriteBatch)batch, getX() + renderer.getWidth() * scale /2, getY(), scale);
		batch.setColor(tmp);
	}

	public void dispose() {
		renderer.dispose();
	}
}
