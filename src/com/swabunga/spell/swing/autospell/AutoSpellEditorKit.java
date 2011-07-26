/*
Jazzy - a Java library for Spell Checking
Copyright (C) 2001 Mindaugas Idzelis
Full text of license can be found in LICENSE.txt

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
/*
 * Created on 2004-apr-23
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.swabunga.spell.swing.autospell;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

/**
 * This editorkit just forwards all method calls to the original EditorKit
 * for all method but getAction where it also adds a "MarkAsMisspelled" action 
 * and getViewFactory where we return our own ViewFactory (Based on the original).
 * 
 * @author Robert Gustavsson (robert@lindesign.se)
 *
 */
@SuppressWarnings("serial")
public class AutoSpellEditorKit extends StyledEditorKit implements AutoSpellConstants{

	private StyledEditorKit editorKit=null;
	private JEditorPane		pane=null;
	
	public AutoSpellEditorKit(StyledEditorKit editorKit){
		this.editorKit=editorKit;
	}
	
	public StyledEditorKit getStyledEditorKit(){
		return editorKit;
	}
	
	@Override
	public Object clone(){
		return new AutoSpellEditorKit(editorKit);
	}
	
	@Override
	public void	deinstall(JEditorPane c){
		editorKit.deinstall(c);
		pane=null;
	}
	
	@Override
	public Element getCharacterAttributeRun(){
		return editorKit.getCharacterAttributeRun();
	}
	
	@Override
	public MutableAttributeSet getInputAttributes(){
		return editorKit.getInputAttributes();
	}
	
	@Override
	public void	install(JEditorPane c){
		editorKit.install(c);
		pane=c;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#getContentType()
	 */
	@Override
	public String getContentType() {
		return editorKit.getContentType();
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#getActions()
	 */
	@Override
	public Action[] getActions() {
		Action[]	actions=new Action[editorKit.getActions().length+1];
		
		for(int i=0;i<editorKit.getActions().length;i++){
			actions[i]=editorKit.getActions()[i];
		}
		actions[actions.length-1]=new SpellCheckAction();
		return actions;
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#createCaret()
	 */
	@Override
	public Caret createCaret() {
		return editorKit.createCaret();
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#createDefaultDocument()
	 */
	@Override
	public Document createDefaultDocument() {
		return editorKit.createDefaultDocument();
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#getViewFactory()
	 */
	@Override
	public ViewFactory getViewFactory() {
		return new AutoSpellViewFactory(editorKit.getViewFactory());
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#read(java.io.InputStream, javax.swing.text.Document, int)
	 */
	@Override
	public void read(InputStream in, Document doc, int pos)
		throws IOException, BadLocationException {
		
		editorKit.read(in, doc, pos);
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#write(java.io.OutputStream, javax.swing.text.Document, int, int)
	 */
	@Override
	public void write(OutputStream out, Document doc, int pos, int len)
		throws IOException, BadLocationException {
		
		editorKit.write(out, doc, pos, len);
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#read(java.io.Reader, javax.swing.text.Document, int)
	 */
	@Override
	public void read(Reader in, Document doc, int pos)
		throws IOException, BadLocationException {
		
		editorKit.read(in, doc, pos);
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.EditorKit#write(java.io.Writer, javax.swing.text.Document, int, int)
	 */
	@Override
	public void write(Writer out, Document doc, int pos, int len)
		throws IOException, BadLocationException {
		
		editorKit.write(out, doc, pos, len);
	}

	// INNER CLASSES
	// ------------------------------------------------------------------
	private class SpellCheckAction extends AbstractAction{
	
		public SpellCheckAction(){
			super("Mark as misspelled");
		}
		
		public void actionPerformed(ActionEvent evt){
			SimpleAttributeSet attr;
			int pos=pane.getCaretPosition();
			if(pos<0)
				return;
			attr=new SimpleAttributeSet(((StyledDocument)pane.getDocument()).getCharacterElement(pos).getAttributes());
			attr.addAttribute(wordMisspelled, wordMisspelledTrue);
			((JTextPane)pane).setCharacterAttributes(attr,false);
		}
	}
}
