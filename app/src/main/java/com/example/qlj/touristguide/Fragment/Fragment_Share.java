package com.example.qlj.touristguide.Fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.qlj.touristguide.Activity.MainActivity;
import com.example.qlj.touristguide.PictureShare.ProcessActivity;
import com.example.qlj.touristguide.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Qlj on 2017/4/17.
 */

public class Fragment_Share extends Fragment {
    private GridView gridView1;
    private Button buttonPublish;
    private final int IMAGE_OPEN = 1;//相册
    private final int GET_DATA = 2;//处理结果
    private final int TAKE_PHOTO = 3;//相机照片
    private String pathImage;
    private Bitmap bmp;
    private Uri imageUri = null;//图片uri地址
    public static String imagePath;
    private String pathTakePhoto;
    private ProgressDialog mpDialog;
    private int count = 0;
    private EditText editText;
    private int flagThread = 0;
    private int flagThreadUpload = 0;
    private int flagThreadDialog = 0;


    private String[] urlPicture;

    private ArrayList<HashMap<String, Object>> imageItem;

    private SimpleAdapter simpleAdapter;

    private String publishIdByJson;


    //获得共享的host和port数据
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_share, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null!=imageUri){
            Bitmap addbmp = null;
            try {
                addbmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imagePath = imageUri.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", addbmp);
            map.put("pathImage", imageUri);
            imageItem.add(map);
            simpleAdapter = new SimpleAdapter(getContext(),
                    imageItem, R.layout.griditem_addpic,
                    new String[] { "itemImage"}, new int[] { R.id.imageView1});
            //�ӿ�����ͼƬ
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data,
                                            String textRepresentation) {
                    // TODO Auto-generated method stub
                    if(view instanceof ImageView && data instanceof Bitmap){
                        ImageView i = (ImageView)view;
                        i.setImageBitmap((Bitmap) data);
                        return true;
                    }
                    return false;
                }
            });
            gridView1.setAdapter(simpleAdapter);
            simpleAdapter.notifyDataSetChanged();
            //ˢ�º��ͷŷ�ֹ�ֻ����ߺ��Զ����
            pathImage = null;
        }

    }

    void initView(View view)
    {
        gridView1 = (GridView) view.findViewById(R.id.gridView1);
        buttonPublish = (Button) view.findViewById(R.id.button1);
        editText = (EditText) view.findViewById(R.id.editText1);


        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imageItem.size()==1) {
                    Toast.makeText(getContext(), "未选中图片", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(null != imageUri)
                {
                    Intent intentPut = new Intent(getContext(), MainActivity.class); //���->����
                    intentPut.putExtra("page",0);
                    startActivity(intentPut);
                }
                Toast.makeText(getContext(), "发布成功", Toast.LENGTH_SHORT).show();

//                String content = editText.getText().toString();
//                startNetThread(pref.getString("host", "0"), Integer.parseInt(pref.getString("port", "0")), content);


            }
        });


        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.gridview_addpic); //�Ӻ�
        imageItem = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("itemImage", bmp);
        map.put("pathImage", "add_pic");
        imageItem.add(map);
        simpleAdapter = new SimpleAdapter(getContext(),
                imageItem, R.layout.griditem_addpic,
                new String[] { "itemImage"}, new int[] { R.id.imageView1});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                // TODO Auto-generated method stub
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView i = (ImageView)view;
                    i.setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
            }
        });
        gridView1.setAdapter(simpleAdapter);


        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                if( imageItem.size() == 10) {
                    Toast.makeText(getContext(), "图片数9张已满", Toast.LENGTH_SHORT).show();
                }
                else if(position == 0) {
                    AddImageDialog();
                }
                else {
                    DeleteDialog(position);
                }

            }
        });


    }

    protected void DeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("确认移除已添加图片吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                imageItem.remove(position);
                simpleAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && requestCode==IMAGE_OPEN) {
            imageUri = data.getData();
            if (!TextUtils.isEmpty(imageUri.getAuthority())) {
//                Cursor cursor = getContext().getContentResolver().query(
//                        uri,
//                        new String[] { MediaStore.Images.Media.DATA },
//                        null,
//                        null,
//                        null);
//                if (null == cursor) {
//                    return;
//                }

//                cursor.moveToFirst();
//                String path = cursor.getString(cursor
//                        .getColumnIndex(MediaStore.Images.Media.DATA));
//                //������������
//                //Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getContext(), ProcessActivity.class); //���->����
//                intent.putExtra("path", path);
//                //startActivity(intent);
//                startActivityForResult(intent, GET_DATA);
            } else {
//                Intent intent = new Intent(getContext(), ProcessActivity.class); //���->����
//                intent.putExtra("path", uri.getPath());
//                //startActivity(intent);
//                startActivityForResult(intent, GET_DATA);
            }
        }  //end if ��ͼƬ

        //获取imageprocess图片
        if(resultCode==RESULT_OK && requestCode==GET_DATA) {
            pathImage = data.getStringExtra("pathProcess");
        }

        //接收相机图片数据，发送到图片处理
        if(resultCode==RESULT_OK && requestCode==TAKE_PHOTO) {
//            Intent intent = new Intent("com.android.camera.action.CROP"); //����
//            intent.setDataAndType(imageUri, "image/*");
//            intent.putExtra("scale", true);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//
//            //发送给处理函数
//            Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            intentBc.setData(imageUri);
//            getContext().sendBroadcast(intentBc);

            //发送到imageprocess
//            Intent intentPut = new Intent(getContext(), ProcessActivity.class); //���->����
//            intentPut.putExtra("path", pathTakePhoto);
//            //startActivity(intent);
//            startActivityForResult(intentPut, GET_DATA);
        }
    }


    protected void AddImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("添加图片");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setCancelable(false);
        builder.setItems(new String[] {"本地相册选择","调用手机照相","取消选择图片"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        switch(which) {
                            case 0: //调用相册
                                dialog.dismiss();
                                Intent intent = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, IMAGE_OPEN);
                                break;
                            case 1: //调用相机
                                dialog.dismiss();
                                File outputImage = new File(Environment.getExternalStorageDirectory(), "suishoupai_image.jpg");
                                pathTakePhoto = outputImage.toString();
                                try {
                                    if(outputImage.exists()) {
                                        outputImage.delete();
                                    }
                                    outputImage.createNewFile();
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                                imageUri = Uri.fromFile(outputImage);
                                Intent intentPhoto = new Intent("android.media.action.IMAGE_CAPTURE"); //����
                                intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intentPhoto, TAKE_PHOTO);
                                break;
                            case 2: //取消
                                dialog.dismiss();
                                break;
                            default:
                                break;
                        }
                    }
                });
        //��ʾ�Ի���
        builder.create().show();
    }


    private void upload_SSP_Pic(final String path,final String dirname) {}


    private void SavePublish(final String type,final String sqlexe) {}


    private void jsonjiexi(String jsondata) {}


    private final int HANDLER_MSG_TELL_RECV = 0x124;
    //上传数据部分
    private void startNetThread(final String host, final int port, final String data) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(host, port);
                    OutputStream outputStream = socket.getOutputStream();

                    /*Resources res = getResources();
                    Bitmap bmp= BitmapFactory.decodeResource(res, R.drawable.picture6);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    outputStream.write(baos.toByteArray());*/
                    outputStream.write((data).getBytes());
                    Log.i("post","ready");
                    outputStream.flush();
                    Log.i("post","success");
                    System.out.println(socket);

                    InputStream is = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int n = is.read(bytes);
                    System.out.println(new String(bytes, 0, n));

                    Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, new String(bytes, 0, n));
                    Log.i("get","ready");
                    msg.sendToTarget();
                    Log.i("get","success");
                    is.close();
                    socket.close();
                } catch (Exception e) {
                    Log.i("Exception",e.toString());
                }
            }
        };

        thread.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("来自服务器的数据：" + (String)msg.obj);
            builder.create().show();
        };
    };
}
