package com.goodchild.zibalba.html;

import com.goodchild.zibalba.core.Zibalba;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class ZibalbaHtml extends GwtApplication {
	@Override
	public ApplicationListener getApplicationListener () {
		return new Zibalba();
	}
	
	@Override
	public GwtApplicationConfiguration getConfig () {
		return new GwtApplicationConfiguration(480, 320);
	}
}
