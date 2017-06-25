package com.fcih.gp.furniturego;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FireBaseHelper {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference myRootRef = database.getReference();
    private static final String TAG = "FirebaseHelper";

    public interface OnGetDataListener<T> {
        void onSuccess(T Data);
    }

    public interface OnGetDataListListener<T> {
        void onSuccess(List<T> Data);
    }

    //region Temp
    private static class ClassName {
        //TODO:-----------------------------------------------------
        //TODO:Add Ref To Tables and make public class & Refactor ClassName
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("TableName");

        public String Key;
        //TODO:Add Columns
        //ex: public String Column;
        public String Column;
        //TODO:Add Foreign
        //ex:public ForeignClass ForeignClass

        public ClassName() {
            //TODO:ForeignClasses
            //ex ForeignClass = new ForeignClass();
        }

        //region Getter & Setter
        //TODO:Create Getter & Setter

        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<ClassName> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //ex: final ClassName obj = new Teams();
                    if (dataSnapshot.exists()) {
                        final ClassName obj = new ClassName();
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        listener.onSuccess(obj);
                    } else {
                        listener.onSuccess(null);
                    }
                    //TODO:Foreign Keys
                    //ex:
                    //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                    //@Override
                    //public void onSuccess(Object Data) {
                    //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                    //    listener.onSuccess(obj);
                    //}
                    //});
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<ClassName> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<ClassName> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final ClassName obj = new ClassName();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                        //TODO:Foreign Keys
                        //ex:
                        //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                        //@Override
                        //public void onSuccess(Object Data) {
                        //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                        //    Items.add(obj);
                        //if (!iterator.hasNext()) {
                        //    listener.onSuccess(Items);
                        //}
                        //}
                        //});
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Tolist(final OnGetDataListListener<ClassName> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<ClassName> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final ClassName obj = new ClassName();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                        //TODO:Foreign Keys
                        //ex:
                        //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                        //@Override
                        //public void onSuccess(Object Data) {
                        //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                        //    Items.add(obj);
                        //if (!iterator.hasNext()) {
                        //    listener.onSuccess(Items);
                        //}
                        //}
                        //});
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(ClassName obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(ClassName obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //TODO:Add Columns
            //ex:Column("ColumnName"),
            Column("ColumnName");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region Categories
    public static class Categories {
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("Categories");

        public String Key;
        //ex: public String Column;
        public String name;
        //ex:public ForeignClass ForeignClass

        public Categories() {
            //ex ForeignClass = new ForeignClass();
        }

        //region Getter & Setter

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<Categories> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ex: final ClassName obj = new Teams();
                        final Categories obj = new Categories();
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        listener.onSuccess(obj);

                    } else {
                        listener.onSuccess(null);
                    }
                    //ex:
                    //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                    //@Override
                    //public void onSuccess(Object Data) {
                    //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                    //    listener.onSuccess(obj);
                    //}
                    //});
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<Categories> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Categories> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Categories obj = new Categories();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                        //ex:
                        //ForeignClass.Findbykey(obj.ForeignKey, new OnGetDataListener() {
                        //@Override
                        //public void onSuccess(Object Data) {
                        //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                        //    Items.add(obj);
                        //if (!iterator.hasNext()) {
                        //    listener.onSuccess(Items);
                        //}
                        //}
                        //});
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Tolist(final OnGetDataListListener<Categories> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Categories> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Categories obj = new Categories();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                        //ex:
                        //ForeignClass.Findbykey(obj.ForeignKey, new OnGetDataListener() {
                        //@Override
                        //public void onSuccess(Object Data) {
                        //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                        //    Items.add(obj);
                        //if (!iterator.hasNext()) {
                        //    listener.onSuccess(Items);
                        //}
                        //}
                        //});
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(Categories obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(Categories obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            Name("name");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region Companies
    public static class Companies {
        //TODO:-----------------------------------------------------
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("Users");

        public String Key;
        //ex: public String Column;
        public String about;
        public String email;
        public String image_uri;
        public String name;
        public String type_id;

        //ex:public ForeignClass ForeignClass
        public User_Types user_types;

        public Companies() {
            //ex ForeignClass = new ForeignClass();
            user_types = new User_Types();
        }

        //region Getter & Setter

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getImage_uri() {
            return image_uri;
        }

        public void setImage_uri(String image_uri) {
            this.image_uri = image_uri;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType_id() {
            return type_id;
        }

        public void setType_id(String type_id) {
            this.type_id = type_id;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<Companies> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ex: final ClassName obj = new Teams();
                        final Companies obj = new Companies();
                        obj.Key = dataSnapshot.getKey();
                        for (Companies.Table T : Companies.Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        //listener.onSuccess(obj);
                        //ex:
                        user_types.Findbykey(obj.type_id, Data -> {
                            obj.user_types = Data;
                            listener.onSuccess(obj);
                        });

                    } else {
                        listener.onSuccess(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Where(Companies.Table table, String Value, final OnGetDataListListener<Companies> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Companies> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Companies obj = new Companies();
                        obj.Key = postSnapshot.getKey();
                        for (Companies.Table T : Companies.Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key

                        user_types.Findbykey(obj.type_id, Data -> {
                            obj.user_types = Data;
                            Items.add(obj);
                            if (!iterator.hasNext()) {
                                listener.onSuccess(Items);
                            }
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Tolist(final OnGetDataListListener<Companies> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Companies> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Companies obj = new Companies();
                        obj.Key = postSnapshot.getKey();
                        for (Companies.Table T : Companies.Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        user_types.Findbykey(obj.type_id, Data -> {
                            obj.user_types = Data;
                            Items.add(obj);
                            if (!iterator.hasNext()) {
                                listener.onSuccess(Items);
                            }
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(Companies obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(Companies obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            About("about"),
            Email("email"),
            Image_uri("image_uri"),
            Name("name"),
            Type_id("type_id");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region Objects
    public static class Objects {
        //TODO:-----------------------------------------------------
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("Objects");

        public String Key;
        //ex: public String Column;
        public String category;
        public String company_id;
        public String date;
        public String description;
        public String gif_path;
        public String image_path;
        public String model_path;
        public String name;
        public String price;
        //ex:public ForeignClass ForeignClass
        public Categories categories;
        public Companies companies;
        public List<Feedbacks> feedbacks;

        public Objects() {
            categories = new Categories();
            companies = new Companies();
            feedbacks = new ArrayList<>();
            //ex ForeignClass = new ForeignClass();
        }

        //region Getter & Setter

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getCompany_id() {
            return company_id;
        }

        public void setCompany_id(String company_id) {
            this.company_id = company_id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getGif_path() {
            return gif_path;
        }

        public void setGif_path(String gif_path) {
            this.gif_path = gif_path;
        }

        public String getImage_path() {
            return image_path;
        }

        public void setImage_path(String image_path) {
            this.image_path = image_path;
        }

        public String getModel_path() {
            return model_path;
        }

        public void setModel_path(String model_path) {
            this.model_path = model_path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<Objects> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ex: final ClassName obj = new Teams();
                        final Objects obj = new Objects();
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        //listener.onSuccess(obj);
                        //ex:
                        companies.Findbykey(obj.company_id, Data -> {
                            obj.companies = Data;
                            categories.Findbykey(obj.category, Data12 -> {
                                obj.categories = Data12;
                                Feedbacks.Ref.orderByChild(Feedbacks.Table.Object_id.text).equalTo(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Feedbacks fed = new Feedbacks();
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            for (Feedbacks.Table T : Feedbacks.Table.values()) {
                                                fed.setbyName(fed, T.name(), data.child(T.text).getValue().toString());
                                                obj.feedbacks.add(fed);
                                            }
                                        }
                                        listener.onSuccess(obj);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "Firebase Warning :" + databaseError);
                                    }
                                });
                            });
                        });

                    } else {
                        listener.onSuccess(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<Objects> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Objects> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Objects obj = new Objects();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        companies.Findbykey(obj.company_id, Data -> {
                            obj.companies = Data;
                            categories.Findbykey(obj.category, Data1 -> {
                                obj.categories = Data1;
                                Feedbacks.Ref.orderByChild(Feedbacks.Table.Object_id.text).equalTo(obj.Key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Feedbacks fed = new Feedbacks();
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            for (Feedbacks.Table T : Feedbacks.Table.values()) {
                                                fed.setbyName(fed, T.name(), data.child(T.text).getValue().toString());
                                                obj.feedbacks.add(fed);
                                            }
                                        }
                                        Items.add(obj);
                                        if (!iterator.hasNext()) {
                                            listener.onSuccess(Items);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "Firebase Warning :" + databaseError);
                                    }
                                });

                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Where(Query query, final OnGetDataListListener<Objects> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Objects> Items = new ArrayList<>();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Objects obj = new Objects();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        companies.Findbykey(obj.company_id, Data -> {
                            obj.companies = Data;
                            categories.Findbykey(obj.category, Data1 -> {
                                obj.categories = Data1;
                                Feedbacks.Ref.orderByChild(Feedbacks.Table.Object_id.text).equalTo(obj.Key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Feedbacks fed = new Feedbacks();
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            for (Feedbacks.Table T : Feedbacks.Table.values()) {
                                                fed.setbyName(fed, T.name(), data.child(T.text).getValue().toString());
                                                obj.feedbacks.add(fed);
                                            }
                                        }
                                        Items.add(obj);
                                        if (!iterator.hasNext()) {
                                            listener.onSuccess(Items);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "Firebase Warning :" + databaseError);
                                    }
                                });

                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Tolist(final OnGetDataListListener<Objects> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Objects> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Objects obj = new Objects();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        companies.Findbykey(obj.company_id, Data -> {
                            obj.companies = Data;
                            categories.Findbykey(obj.category, Data1 -> {
                                obj.categories = Data1;
                                Feedbacks.Ref.orderByChild(Feedbacks.Table.Object_id.text).equalTo(obj.Key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Feedbacks fed = new Feedbacks();
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            for (Feedbacks.Table T : Feedbacks.Table.values()) {
                                                fed.setbyName(fed, T.name(), data.child(T.text).getValue().toString());
                                                obj.feedbacks.add(fed);
                                            }
                                        }
                                        Items.add(obj);
                                        if (!iterator.hasNext()) {
                                            listener.onSuccess(Items);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "Firebase Warning :" + databaseError);
                                    }
                                });
                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(Objects obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(Objects obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            Category("category"),
            Company_id("company_id"),
            Date("date"),
            Description("description"),
            Model_path("model_path"),
            Gif_path("gif_path"),
            Name("name"),
            Price("price"),
            Image_path("image_path");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region Users
    public static class Users {
        //TODO:-----------------------------------------------------
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("Users");

        public String Key;
        //ex: public String Column;
        public String email;
        public String image_uri;
        public String name;
        public String type_id;
        //ex:public ForeignClass ForeignClass
        public User_Types user_types;

        public Users() {
            user_types = new User_Types();
            //ex ForeignClass = new ForeignClass();
        }

        //region Getter & Setter

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getImage_uri() {
            return image_uri;
        }

        public void setImage_uri(String image_uri) {
            this.image_uri = image_uri;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType_id() {
            return type_id;
        }

        public void setType_id(String type_id) {
            this.type_id = type_id;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<Users> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ex: final ClassName obj = new Teams();
                        final Users obj = new Users();
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        //listener.onSuccess(obj);
                        //ex:
                        user_types.Findbykey(obj.type_id, Data -> {
                            obj.user_types = Data;
                            listener.onSuccess(obj);
                        });

                    } else {
                        listener.onSuccess(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<Users> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Users> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Users obj = new Users();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key

                        user_types.Findbykey(obj.type_id, Data -> {
                            obj.user_types = Data;
                            Items.add(obj);
                            if (!iterator.hasNext()) {
                                listener.onSuccess(Items);
                            }
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Tolist(final OnGetDataListListener<Users> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Users> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Users obj = new Users();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        user_types.Findbykey(obj.type_id, Data -> {
                            obj.user_types = Data;
                            Items.add(obj);
                            if (!iterator.hasNext()) {
                                listener.onSuccess(Items);
                            }
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(Users obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(Users obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            Email("email"),
            Image_uri("image_uri"),
            Name("name"),
            Type_id("type_id");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region User_Types
    public static class User_Types {
        //TODO:-----------------------------------------------------
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("User_Types");

        public String Key;
        //ex: public String Column;
        public String name;
        //ex:public ForeignClass ForeignClass

        public User_Types() {
            //ex ForeignClass = new ForeignClass();
        }

        //region Getter & Setter

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<User_Types> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ex: final ClassName obj = new Teams();
                        final User_Types obj = new User_Types();
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        listener.onSuccess(obj);

                    } else {
                        listener.onSuccess(null);
                    }
                    //ex:
                    //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                    //@Override
                    //public void onSuccess(Object Data) {
                    //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                    //    listener.onSuccess(obj);
                    //}
                    //});
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<User_Types> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<User_Types> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final User_Types obj = new User_Types();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                        //ex:
                        //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                        //@Override
                        //public void onSuccess(Object Data) {
                        //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                        //    Items.add(obj);
                        //if (!iterator.hasNext()) {
                        //    listener.onSuccess(Items);
                        //}
                        //}
                        //});
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Tolist(final OnGetDataListListener<User_Types> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<User_Types> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final User_Types obj = new User_Types();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        Items.add(obj);
                        if (!iterator.hasNext()) {
                            listener.onSuccess(Items);
                        }
                        //ex:
                        //ForeignClass.findbykey(obj.ForeignKey, new OnGetDataListener() {
                        //@Override
                        //public void onSuccess(Object Data) {
                        //    obj.ForeignClass = (FireBaseHelper.ForeignClass) Data;
                        //    Items.add(obj);
                        //if (!iterator.hasNext()) {
                        //    listener.onSuccess(Items);
                        //}
                        //}
                        //});
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Firebase Warning :" + databaseError);
                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(User_Types obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(User_Types obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            Name("name");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region Feedbacks
    public static class Feedbacks {
        //TODO:-----------------------------------------------------
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("Feedbacks");

        public String Key;
        //ex: public String Column;
        public String date;
        public String feedback;
        public String object_id;
        public String rate;
        public String uid;
        //ex:public ForeignClass ForeignClass
        public Objects objects;
        public Users users;

        public Feedbacks() {
            //ex ForeignClass = new ForeignClass();
            objects = new Objects();
            users = new Users();
        }

        //region Getter & Setter

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public String getObject_id() {
            return object_id;
        }

        public void setObject_id(String object_id) {
            this.object_id = object_id;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<Feedbacks> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ex: final ClassName obj = new Teams();
                        final Feedbacks obj = new Feedbacks();
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        //listener.onSuccess(obj);
                        //ex:
                        objects.Findbykey(obj.object_id, Data -> {
                            obj.objects = Data;
                            users.Findbykey(obj.uid, Data1 -> {
                                obj.users = Data1;
                                listener.onSuccess(obj);
                            });
                        });

                    } else {
                        listener.onSuccess(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<Feedbacks> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Feedbacks> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Feedbacks obj = new Feedbacks();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }

                        objects.Findbykey(obj.object_id, Data -> {
                            obj.objects = Data;
                            users.Findbykey(obj.uid, Data1 -> {
                                obj.users = Data1;
                                Items.add(obj);
                                if (!iterator.hasNext()) {
                                    listener.onSuccess(Items);
                                }
                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Tolist(final OnGetDataListListener<Feedbacks> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Feedbacks> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Feedbacks obj = new Feedbacks();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        objects.Findbykey(obj.object_id, Data -> {
                            obj.objects = Data;
                            users.Findbykey(obj.uid, Data1 -> {
                                obj.users = Data1;
                                Items.add(obj);
                                if (!iterator.hasNext()) {
                                    listener.onSuccess(Items);
                                }
                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(Feedbacks obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(Feedbacks obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            Date("date"),
            Feedback("feedback"),
            Object_id("object_id"),
            Rate("rate"),
            Uid("uid");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion

    //region Favorites
    public static class Favorites {
        //TODO:-----------------------------------------------------
        //ex: private static final DatabaseReference Ref = myRootRef.child("TableName");
        public static final DatabaseReference Ref = myRootRef.child("Favorites");

        public String Key;
        //ex: public String Column;
        public String object_id;
        public String user_id;
        //ex:public ForeignClass ForeignClass
        public Objects objects;
        public Users users;

        public Favorites() {
            //ex ForeignClass = new ForeignClass();
            objects = new Objects();
            users = new Users();
        }

        //region Getter & Setter

        public String getObject_id() {
            return object_id;
        }

        public void setObject_id(String object_id) {
            this.object_id = object_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }


        //endregion

        public String Add() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            DatabaseReference myref = Ref.push();
            myref.setValue(Values);
            return Key = myref.getKey();

        }

        public void Add(String Key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update() {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(Key).setValue(Values);
        }

        public void Update(String key) {
            Map<String, String> Values = new HashMap<>();
            for (Table T : Table.values()) {
                Values.put(T.text, getbyName(this, T.name()));
            }
            Ref.child(key).setValue(Values);
        }

        public void Findbykey(String key, final OnGetDataListener<Favorites> listener) {
            Ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //ex: final ClassName obj = new Teams();
                    final Favorites obj = new Favorites();
                    if (dataSnapshot.exists()) {
                        obj.Key = dataSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), dataSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        //listener.onSuccess(obj);
                        //ex:
                        objects.Findbykey(obj.object_id, Data -> {
                            obj.objects = Data;
                            users.Findbykey(obj.user_id, Data1 -> {
                                obj.users = Data1;
                                listener.onSuccess(obj);
                            });
                        });
                    } else {
                        listener.onSuccess(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Where(Table table, String Value, final OnGetDataListListener<Favorites> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Favorites> Items = new ArrayList<>();
            Query query = Ref.orderByChild(table.text).equalTo(Value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Favorites obj = new Favorites();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }

                        objects.Findbykey(obj.object_id, Data -> {
                            obj.objects = Data;
                            users.Findbykey(obj.user_id, Data1 -> {
                                obj.users = Data1;
                                Items.add(obj);
                                if (!iterator.hasNext()) {
                                    listener.onSuccess(Items);
                                }
                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Tolist(final OnGetDataListListener<Favorites> listener) {
            //ex: final List<ClassName> Items = new ArrayList<>();
            final List<Favorites> Items = new ArrayList<>();
            Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot postSnapshot = (DataSnapshot) iterator.next();
                        //ex: final ClassName obj = new Teams();
                        final Favorites obj = new Favorites();
                        obj.Key = postSnapshot.getKey();
                        for (Table T : Table.values()) {
                            setbyName(obj, T.name(), postSnapshot.child(T.text).getValue().toString());
                        }
                        //if no foreign key
                        objects.Findbykey(obj.object_id, Data -> {
                            obj.objects = Data;
                            users.Findbykey(obj.user_id, Data1 -> {
                                obj.users = Data1;
                                Items.add(obj);
                                if (!iterator.hasNext()) {
                                    listener.onSuccess(Items);
                                }
                            });
                        });
                    }
                    if (dataSnapshot.getChildrenCount() == 0) {
                        listener.onSuccess(Items);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void Remove(String Key) {
            Ref.child(Key).removeValue();
        }

        private String getbyName(Favorites obj, String Name) {
            String Value = "";
            try {
                Method method = getClass().getDeclaredMethod("get" + Name);
                Object value = method.invoke(obj);
                Value = (String) value;

            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
            return Value;
        }

        private void setbyName(Favorites obj, String Name, String Value) {
            try {
                Class[] cArg = new Class[1];
                cArg[0] = String.class;
                Method method = getClass().getDeclaredMethod("set" + Name, cArg);
                method.invoke(obj, Value);
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "Firebase Invoke Error :" + e.getMessage(), e);
            }
        }

        public enum Table {
            //ex:Column("ColumnName"),
            Object_id("object_id"),
            User_id("user_id");

            public final String text;

            Table(final String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return text;
            }
        }
    }
    //endregion
}
