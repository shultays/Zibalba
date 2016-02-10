package com.goodchild.zibalba.core;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.goodchild.zibalba.packets.CreateTablePacket;
import com.goodchild.zibalba.packets.Packet;


public class Zibalba implements ApplicationListener, ServerThread.Listener {

    ServerThread server;
    private Stage stage;

    Texture up, down;
    TextButton.TextButtonStyle style;
    Table table;

	@Override
	public void create () {
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        up = new Texture(Gdx.files.internal("grayButton.png"));
        down = new Texture(Gdx.files.internal("grayButtonH.png"));
        BitmapFont buttonFont = new BitmapFont(Gdx.files.internal("26pt_gray.fnt"),
                Gdx.files.internal("26pt_gray.png"), false);

        style = new TextButton.TextButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(up));
        style.down = new TextureRegionDrawable(new TextureRegion(down));
        style.font = buttonFont;

        server = new ServerThread(this);

        TextButton createServer = getButton("Create Server", 10);
        createServer.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                CreateTablePacket createTablePacket = new CreateTablePacket();
                server.sendPacket(CreateTablePacket.PACKET_ID, createTablePacket);
            }
        });
        table.add(createServer);

	}

    public TextButton getButton(String string, int y){
        TextButton btn = new TextButton(string, style);
        btn.setPosition((stage.getWidth()-up.getWidth())/2, y);
        return btn;
    }
    public void resize (int width, int height) {
        stage.setViewport(width, height, true);
    }

    public void render () {
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        Table.drawDebug(stage); // This is optional, but enables debug lines for tables.
    }

    public void dispose() {
        stage.dispose();
    }

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

    @Override
    public void packetReceived(int packetID, Packet packet, String packetString) {
        System.out.println(packetID+"<<" + packetString);
    }
}
