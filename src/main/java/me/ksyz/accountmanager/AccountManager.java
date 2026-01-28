/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraftforge.common.MinecraftForge
 */
package me.ksyz.accountmanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import me.ksyz.accountmanager.Events;
import me.ksyz.accountmanager.auth.Account;
import me.ksyz.accountmanager.utils.Nan0EventRegister;
import me.ksyz.accountmanager.utils.SSLUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class AccountManager {
    private static final Minecraft mc = Minecraft.func_71410_x();
    private static final File file = new File(AccountManager.mc.field_71412_D, "openmyau.accounts.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final ArrayList<Account> accounts = new ArrayList();

    public static void init() {
        SSLContext ignored = SSLUtils.getSSLContext();
        Nan0EventRegister.register(MinecraftForge.EVENT_BUS, new Events());
        if (!file.exists()) {
            try {
                if ((file.getParentFile().exists() || file.getParentFile().mkdirs()) && file.createNewFile()) {
                    System.out.print("Successfully created openmyau.accounts.json!");
                }
            }
            catch (IOException e) {
                System.err.print("Couldn't create openmyau.accounts.json!");
            }
        }
    }

    public static void load() {
        accounts.clear();
        try {
            JsonElement json = new JsonParser().parse((Reader)new BufferedReader(new FileReader(file)));
            if (json instanceof JsonArray) {
                JsonArray jsonArray = json.getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    accounts.add(new Account(Optional.ofNullable(jsonObject.get("refreshToken")).map(JsonElement::getAsString).orElse(""), Optional.ofNullable(jsonObject.get("accessToken")).map(JsonElement::getAsString).orElse(""), Optional.ofNullable(jsonObject.get("username")).map(JsonElement::getAsString).orElse(""), Optional.ofNullable(jsonObject.get("unban")).map(JsonElement::getAsLong).orElse(0L), Optional.ofNullable(jsonObject.get("clientId")).map(JsonElement::getAsString).orElse(""), Optional.ofNullable(jsonObject.get("scope")).map(JsonElement::getAsString).orElse("")));
                }
            }
        }
        catch (FileNotFoundException e) {
            System.err.print("Couldn't find openmyau.accounts.json!");
        }
    }

    public static void save() {
        try {
            JsonArray jsonArray = new JsonArray();
            for (Account account : accounts) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("refreshToken", account.getRefreshToken());
                jsonObject.addProperty("accessToken", account.getAccessToken());
                jsonObject.addProperty("username", account.getUsername());
                jsonObject.addProperty("unban", (Number)account.getUnban());
                jsonObject.addProperty("clientId", account.getClientId());
                jsonObject.addProperty("scope", account.getScope());
                jsonArray.add((JsonElement)jsonObject);
            }
            PrintWriter printWriter = new PrintWriter(new FileWriter(file));
            printWriter.println(gson.toJson((JsonElement)jsonArray));
            printWriter.close();
        }
        catch (IOException e) {
            System.err.print("Couldn't save openmyau.accounts.json!");
        }
    }
}

