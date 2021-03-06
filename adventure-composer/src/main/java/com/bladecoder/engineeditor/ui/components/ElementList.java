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
package com.bladecoder.engineeditor.ui.components;

import java.util.Comparator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bladecoder.engineeditor.model.BaseDocument;
import com.bladecoder.engineeditor.utils.I18NUtils;

public abstract class ElementList extends EditList<Element> {

	protected BaseDocument doc;
	protected Element parent;

	protected Element clipboard;
	
	private boolean sorted;

	public ElementList(Skin skin, boolean sorted) {
		super(skin);
		
		list.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int pos = list.getSelectedIndex();

				toolbar.disableEdit(pos == -1);
			}
		});
		
		this.sorted = sorted;

	}

	public void addElements(BaseDocument doc, Element parent, String tag) {
		this.doc = doc;
		this.parent = parent;

		list.getItems().clear();
		list.getSelection().clear();

		if (parent != null) {

			NodeList nl;
			
			if(tag == null)
				nl = parent.getChildNodes();
			else {
//				nl = parent.getElementsByTagName(tag);
				nl = doc.getChildrenByTag(parent, tag);
			}

			for (int i = 0; i < nl.getLength(); i++) {
				if(nl.item(i) instanceof Element)
					addItem((Element) nl.item(i));
			}			
		}
		
		if (getItems().size > 0)
			list.setSelectedIndex(0);
		
		toolbar.disableEdit(list.getSelectedIndex() < 0);
		
		if(sorted) {
			list.getItems().sort(new Comparator<Element>() {
				@Override
				public int compare(Element o1, Element o2) {
					return o1.getAttribute("id").compareTo(o2.getAttribute("id"));
				}
			});
		}

		toolbar.disableCreate(parent == null);
//		list.setWidth(getWidth());
		list.invalidateHierarchy();
	}

	@Override
	protected void create() {
		EditElementDialog dialog = getEditElementDialogInstance(null);
		dialog.show(getStage());
		dialog.setListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Element e = ((EditElementDialog)actor).getElement();
				addItem(e);
				int i = getItems().indexOf(e, true);
				if(i != -1)
					list.setSelectedIndex(i);
				
				list.invalidateHierarchy();
			}			
		});
	}

	@Override
	protected void edit() {

		Element e = list.getSelected();

		if (e == null)
			return;


		EditElementDialog dialog = getEditElementDialogInstance(e);
		dialog.show(getStage());
		dialog.setListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Element e = ((EditElementDialog)actor).getElement();
				doc.setModified(e);
			}			
		});		
	}

	protected abstract EditElementDialog getEditElementDialogInstance(Element e);

	@Override
	protected void delete() {
		int pos = list.getSelectedIndex();

		if (pos == -1)
			return;

		Element e = list.getItems().removeIndex(pos);

		doc.deleteElement(e);

		clipboard = e;
		I18NUtils.putTranslationsInElement(doc, clipboard);
		toolbar.disablePaste(false);

		if (pos > 0)
			list.setSelectedIndex(pos - 1);
		else if (pos == 0 && list.getItems().size > 0)
			list.setSelectedIndex(0);
//		else
//			list.clearSelection();
	}

	@Override
	protected void copy() {
		Element e = list.getSelected();

		if (e == null)
			return;

		clipboard = (Element) e.cloneNode(true);
		I18NUtils.putTranslationsInElement(doc, clipboard);
		toolbar.disablePaste(false);
	}

	@Override
	protected void paste() {
		Element newElement = doc.cloneNode(parent, clipboard);
		int pos = list.getSelectedIndex() + 1;
		
		Element e2 = null;	
		
		if(pos!=0 && pos < list.getItems().size) 
			e2 = list.getItems().get(pos);
		
		list.getItems().insert(pos, newElement);
		
		Node parent = newElement.getParentNode();
		parent.insertBefore(newElement, e2);
		I18NUtils.extractStrings(doc, newElement);
		
		list.setSelectedIndex(pos);		
		list.invalidateHierarchy();
	}
}
