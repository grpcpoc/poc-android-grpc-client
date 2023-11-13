package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drathveloper.grpc.CreatedUser;
import com.drathveloper.grpc.User;
import com.drathveloper.grpc.UserAddress;
import com.drathveloper.grpc.UserBulkLoadRequest;
import com.drathveloper.grpc.UserBulkLoadResponse;
import com.drathveloper.grpc.UserServiceGrpc;
import com.example.myapplication.arrayadapter.CreatedUserAdapter;
import com.example.myapplication.arrayadapter.UserAdapter;
import com.google.protobuf.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class MainActivity extends AppCompatActivity {

    private static final String UNARY_MODE = "unaryMode";
    private static final String SERVER_MODE = "serverMode";
    private static final String CLIENT_MODE = "clientMode";
    private static final String BIDIRECTIONAL_MODE = "bidirectionalMode";
    private String actualMode = UNARY_MODE;

    private static final String HOST = "10.0.2.2";
    private static final int PORT = 50052;
    ManagedChannelBuilder mChannel;

    UserServiceGrpc.UserServiceStub userServiceStub;
    UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;
    EditText hostEdit;
    EditText portEdit;
    EditText messageEdit;
    TextView resultText;
    Button submitButton;

    UserAdapter userAdapter;
    CreatedUserAdapter createdUserAdapter;

    ListView listView1;
    ListView listView2;

    ArrayList<User> arrayOfUsers;
    ArrayList<CreatedUser> arrayOfCreatedUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannel = ManagedChannelBuilder.forAddress(HOST, PORT).usePlaintext();
        userServiceStub = UserServiceGrpc.newStub(mChannel.build());
        userServiceBlockingStub = UserServiceGrpc.newBlockingStub(mChannel.build());
        setContentView(R.layout.activity_main);

        listView1 = (ListView) findViewById(R.id.listView1);
        listView2 = (ListView) findViewById(R.id.listView2);

        arrayOfUsers = new ArrayList<User>();
        userAdapter = new UserAdapter(this, arrayOfUsers);
        listView1.setAdapter(userAdapter);

        arrayOfCreatedUsers = new ArrayList<CreatedUser>();
        createdUserAdapter = new CreatedUserAdapter(this, arrayOfCreatedUsers);
        listView2.setAdapter(createdUserAdapter);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.println(Log.INFO, "Info", String.valueOf(checkedId));
                cleanAdapters(userAdapter, createdUserAdapter);
                if (checkedId == R.id.unaryRadio) {
                    actualMode = UNARY_MODE;
                } else if (checkedId == R.id.serverRadio) {
                    actualMode = SERVER_MODE;
                } else if (checkedId == R.id.clientRadio) {
                    actualMode = CLIENT_MODE;
                } else if (checkedId == R.id.bidirectionalRadio) {
                    actualMode = BIDIRECTIONAL_MODE;
                }
            }
        });


    }

    public void onExecute(View view) {
        cleanAdapters(userAdapter, createdUserAdapter);
        try {
            switch (actualMode) {
                case UNARY_MODE:
                    bulkLoadCreateUserUnary();
                    break;
                case SERVER_MODE:
                    bulkLoadCreateUserServerStream();
                    break;
                case CLIENT_MODE:
                    bulkLoadCreateUserClientStream();
                    break;
                case BIDIRECTIONAL_MODE:
                    bulkLoadCreateUserBidirectionalStream();
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void cleanAdapters(UserAdapter userAdapter, CreatedUserAdapter createdUserAdapter) {
        userAdapter.clear();
        createdUserAdapter.clear();
    }


    public void bulkLoadCreateUserUnary() throws InterruptedException {

        UserBulkLoadRequest request = generateBulkLoadGrpcRequest(5);
        userAdapter.addAll(request.getUsersList());

        StreamObserver streamObserver = new StreamObserver<UserBulkLoadResponse>() {
            @Override
            public void onNext(UserBulkLoadResponse value) {
                Log.println(Log.VERBOSE, "Info", value.getCreatedUsersList().toString());
                runOnUiThread(new Runnable() {
                    //El hilo se aplica para que sea mas presentable y entendible para la demo , en futuros proyectos se puede quitar
                    @Override
                    public void run() {
                        createdUserAdapter.addAll(value.getCreatedUsersList());
                        createdUserAdapter.notifyDataSetChanged();
                        createdUserAdapter.notifyDataSetInvalidated();
                    }
                });

            }

            @Override
            public void onError(Throwable t) {
                Log.println(Log.ERROR, "Error", "Error en la llamada ");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                Log.println(Log.INFO, "Info", "Llamada completada ");
            }
        };

        userServiceStub.bulkLoad(request, streamObserver);

    }

    public void bulkLoadCreateUserServerStream() throws InterruptedException {

        UserBulkLoadRequest request = generateBulkLoadGrpcRequest(5);
        userAdapter.addAll(request.getUsersList());

        StreamObserver streamObserver = new StreamObserver<CreatedUser>() {
            @Override
            public void onNext(CreatedUser value) {
                Log.println(Log.VERBOSE, "Next", value.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createdUserAdapter.add(value);
                        createdUserAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                Log.println(Log.ERROR, "Error", "Error en la llamada ");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                Log.println(Log.INFO, "Completed", "Llamada completada ");
            }
        };
        userServiceStub.bulkLoadServerStream(request, streamObserver);
    }

    public void bulkLoadCreateUserClientStream() throws InterruptedException {

        UserBulkLoadRequest request = generateBulkLoadGrpcRequest(5);
        //userAdapter.addAll(request.getUsersList());
        StreamObserver streamObserver = new StreamObserver<UserBulkLoadResponse>() {
            @Override
            public void onNext(UserBulkLoadResponse value) {
                Log.println(Log.VERBOSE, "Next", value.toString());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createdUserAdapter.addAll(value.getCreatedUsersList());
                        createdUserAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                Log.println(Log.ERROR, "Error", "Error en la llamada ");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                Log.println(Log.INFO, "Completed", "Llamada completada ");
            }
        };
        StreamObserver<User> observerUsers = userServiceStub.bulkLoadClientStream(streamObserver);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        observerUsers.onNext(request.getUsers(userAdapter.getCount()));
                        userAdapter.add(request.getUsers(userAdapter.getCount()));
                        if(userAdapter.getCount() == 5){
                            timer.cancel();
                            observerUsers.onCompleted();
                        }
                    }
                });

            }
        };

        timer.schedule(task, 0, 1000);


    }


    public void bulkLoadCreateUserBidirectionalStream() throws InterruptedException {
        UserBulkLoadRequest request = generateBulkLoadGrpcRequest(5);

        StreamObserver streamObserver = new StreamObserver<CreatedUser>() {
            @Override
            public void onNext(CreatedUser value) {
                Log.println(Log.VERBOSE, "Next", value.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createdUserAdapter.add(value);
                        createdUserAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                Log.println(Log.ERROR, "Error", "Error en la llamada ");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                Log.println(Log.INFO, "Completed", "Llamada completada ");
            }
        };
        StreamObserver<User> observerUsers = userServiceStub.bulkLoadBidirectionalStream(streamObserver);

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        observerUsers.onNext(request.getUsers(userAdapter.getCount()));
                        userAdapter.add(request.getUsers(userAdapter.getCount()));
                        if(userAdapter.getCount() == 5){
                            timer.cancel();
                            observerUsers.onCompleted();
                        }
                    }
                });

            }
        };

        timer.schedule(task, 0, 1000);
    }

    public static UserBulkLoadRequest generateBulkLoadGrpcRequest(int numUsers) {
        return UserBulkLoadRequest.newBuilder().addAllUsers(generateUsers(5)).build();
    }

    public static List<User> generateUsers(int numUsers) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < numUsers; i++) {
            users.add(
                    User.newBuilder()
                            .setUsername("someUsername" + i)
                            .setFirstName("name" + i)
                            .setLastName("lastName" + i)
                            .setEmail("email" + i + "@email.com")
                            .setPhone("+34666" + "-" + i)
                            .setBirthDate(Timestamp.newBuilder()
                                    .setSeconds(System.currentTimeMillis() / 1000)
                                    .build())
                            .setAddress(UserAddress.newBuilder()
                                    .setCountry("Spain")
                                    .setCity("Madrid")
                                    .setState("Madrid")
                                    .setAddress("Avenida Ciudad de Barcelona 23, 4B")
                                    .setPostalCode("28007")
                                    .build())
                            .build());
        }
        return users;
    }

}