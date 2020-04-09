package com.kodgem.ar;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        FirebaseStorage storage =FirebaseStorage.getInstance();
        StorageReference modelReference = storage.getReference().child("out.glb");

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);

        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File file =File.createTempFile("out","glb");
                    modelReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            buildTreeDModel(file);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        if (arFragment != null) {
            arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
                AnchorNode anchorNode=new AnchorNode(hitResult.createAnchor());
                anchorNode.setRenderable(renderable);
                arFragment.getArSceneView().getScene().addChild(anchorNode);
            }));
        }
    }


    private ModelRenderable renderable;

    private void buildTreeDModel(File file) {
        RenderableSource renderableSource =RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable
                .builder()
                .setSource(this,renderableSource)
                .setRegistryId(file.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    renderable=modelRenderable;
                });
    }
}
