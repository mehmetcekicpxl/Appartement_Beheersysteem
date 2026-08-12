package com.example.apartmanyonetim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Deze klasse beheert de SQLite-database en versiebeheer
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database naam en versie constanten
    private static final String DATABASE_NAAM = "complex_beheer.db";
    private static final int DATABASE_VERSIE = 5; // Versie verhoogd naar 5

    // Tabelnamen definities
    public static final String TABEL_APPARTEMENTEN = "appartementen";
    public static final String TABEL_HUURDERS = "huurders";
    public static final String TABEL_TRANSACTIES = "transacties";
    public static final String TABEL_NOTITIES = "notities";
    public static final String TABEL_BESTANDEN = "bestanden";

    // Kolomnamen voor Appartementen
    public static final String KOLOM_APPARTEMENT_ID = "id";
    public static final String KOLOM_DEURNUMMER = "deurnummer";
    public static final String KOLOM_VERDIEPING = "verdieping";
    public static final String KOLOM_HUIDIG_SALDO = "huidig_saldo";
    public static final String KOLOM_HUURDER_NAAM = "tenant_name";
    public static final String KOLOM_HUUR_BEDRAG = "rent_amount";
    public static final String KOLOM_AANTAL_BEWONERS = "resident_count"; // Nieuw v4
    public static final String KOLOM_ADRES = "address"; // Nieuw v4

    // Kolomnamen voor Huurders
    public static final String KOLOM_HUURDER_ID = "id";
    public static final String KOLOM_HUURDER_APP_ID = "appartement_id";
    public static final String KOLOM_VOLLEDIGE_NAAM = "volledige_naam";
    public static final String KOLOM_GEZINSGROOTTE = "gezinsgrootte";
    public static final String KOLOM_CONTRACT_DATUM = "contract_datum";
    public static final String KOLOM_CONTRACT_DUUR = "contract_duur_maanden";

    // Kolomnamen voor Transacties
    public static final String KOLOM_TRANSACTIE_ID = "id";
    public static final String KOLOM_TRANSACTIE_APP_ID = "appartement_id"; 
    public static final String KOLOM_TYPE = "type"; 
    public static final String KOLOM_CATEGORIE = "categorie";
    public static final String KOLOM_BEDRAG = "bedrag";
    public static final String KOLOM_DATUM = "datum";
    public static final String KOLOM_IS_BETAALD = "is_betaald";
    public static final String KOLOM_OMSCHRIJVING = "omschrijving";
    public static final String KOLOM_IS_RECURRING = "is_recurring"; // Nieuw v5

    // Kolomnamen voor Notities
    public static final String KOLOM_NOTITIE_ID = "id";
    public static final String KOLOM_NOTITIE_APP_ID = "appartement_id";
    public static final String KOLOM_INHOUD = "inhoud";
    public static final String KOLOM_AANGEMAAKT_OP = "aangemaakt_op";

    // Kolomnamen voor Bestanden
    public static final String KOLOM_BESTAND_ID = "id";
    public static final String KOLOM_BESTAND_APP_ID = "appartement_id";
    public static final String KOLOM_URI_PAD = "uri_pad";
    public static final String KOLOM_BESTANDSTYPE = "bestandstype";

    // SQL-query om de Appartementen-tabel aan te maken
    private static final String MAAK_TABEL_APPARTEMENTEN = "CREATE TABLE " + TABEL_APPARTEMENTEN + " (" +
            KOLOM_APPARTEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KOLOM_DEURNUMMER + " INTEGER, " +
            KOLOM_VERDIEPING + " INTEGER, " +
            KOLOM_HUIDIG_SALDO + " REAL DEFAULT 0, " +
            KOLOM_HUURDER_NAAM + " TEXT, " +
            KOLOM_HUUR_BEDRAG + " REAL DEFAULT 0, " +
            KOLOM_AANTAL_BEWONERS + " INTEGER DEFAULT 0, " +
            KOLOM_ADRES + " TEXT)";

    // SQL-query om de Huurders-tabel aan te maken
    private static final String MAAK_TABEL_HUURDERS = "CREATE TABLE " + TABEL_HUURDERS + " (" +
            KOLOM_HUURDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KOLOM_HUURDER_APP_ID + " INTEGER, " +
            KOLOM_VOLLEDIGE_NAAM + " TEXT, " +
            KOLOM_GEZINSGROOTTE + " INTEGER, " +
            KOLOM_CONTRACT_DATUM + " TEXT, " +
            KOLOM_CONTRACT_DUUR + " INTEGER, " +
            "FOREIGN KEY(" + KOLOM_HUURDER_APP_ID + ") REFERENCES " + TABEL_APPARTEMENTEN + "(" + KOLOM_APPARTEMENT_ID + "))";

    // SQL-query om de Transacties-tabel aan te maken
    private static final String MAAK_TABEL_TRANSACTIES = "CREATE TABLE " + TABEL_TRANSACTIES + " (" +
            KOLOM_TRANSACTIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KOLOM_TRANSACTIE_APP_ID + " INTEGER, " +
            KOLOM_TYPE + " TEXT, " +
            KOLOM_CATEGORIE + " TEXT, " +
            KOLOM_BEDRAG + " REAL, " +
            KOLOM_DATUM + " TEXT, " +
            KOLOM_IS_BETAALD + " INTEGER DEFAULT 0, " +
            KOLOM_OMSCHRIJVING + " TEXT, " +
            KOLOM_IS_RECURRING + " INTEGER DEFAULT 0, " + // Nieuw v5
            "FOREIGN KEY(" + KOLOM_TRANSACTIE_APP_ID + ") REFERENCES " + TABEL_APPARTEMENTEN + "(" + KOLOM_APPARTEMENT_ID + "))";

    // SQL-query om de Notities-tabel aan te maken
    private static final String MAAK_TABEL_NOTITIES = "CREATE TABLE " + TABEL_NOTITIES + " (" +
            KOLOM_NOTITIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KOLOM_NOTITIE_APP_ID + " INTEGER, " +
            KOLOM_INHOUD + " TEXT, " +
            KOLOM_AANGEMAAKT_OP + " TEXT, " +
            "FOREIGN KEY(" + KOLOM_NOTITIE_APP_ID + ") REFERENCES " + TABEL_APPARTEMENTEN + "(" + KOLOM_APPARTEMENT_ID + "))";

    // SQL-query om de Bestanden-tabel aan te maken
    private static final String MAAK_TABEL_BESTANDEN = "CREATE TABLE " + TABEL_BESTANDEN + " (" +
            KOLOM_BESTAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            KOLOM_BESTAND_APP_ID + " INTEGER, " +
            KOLOM_URI_PAD + " TEXT, " +
            KOLOM_BESTANDSTYPE + " TEXT, " +
            "FOREIGN KEY(" + KOLOM_BESTAND_APP_ID + ") REFERENCES " + TABEL_APPARTEMENTEN + "(" + KOLOM_APPARTEMENT_ID + "))";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAAM, null, DATABASE_VERSIE);
    }

    // Wordt aangeroepen wanneer de database voor het eerst wordt gemaakt
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MAAK_TABEL_APPARTEMENTEN);
        db.execSQL(MAAK_TABEL_HUURDERS);
        db.execSQL(MAAK_TABEL_TRANSACTIES);
        db.execSQL(MAAK_TABEL_NOTITIES);
        db.execSQL(MAAK_TABEL_BESTANDEN);
    }

    // Wordt aangeroepen wanneer de databaseversie verandert
    @Override
    public void onUpgrade(SQLiteDatabase db, int oudeVersie, int nieuweVersie) {
        if (oudeVersie < 2) {
            db.execSQL("ALTER TABLE " + TABEL_APPARTEMENTEN + " ADD COLUMN " + KOLOM_HUURDER_NAAM + " TEXT");
        }
        if (oudeVersie < 3) {
             db.execSQL("ALTER TABLE " + TABEL_APPARTEMENTEN + " ADD COLUMN " + KOLOM_HUUR_BEDRAG + " REAL DEFAULT 0");
        }
        if (oudeVersie < 4) {
             // Upgrade naar v4: resident count en address
             db.execSQL("ALTER TABLE " + TABEL_APPARTEMENTEN + " ADD COLUMN " + KOLOM_AANTAL_BEWONERS + " INTEGER DEFAULT 0");
             db.execSQL("ALTER TABLE " + TABEL_APPARTEMENTEN + " ADD COLUMN " + KOLOM_ADRES + " TEXT");
        }
        if (oudeVersie < 5) {
             // Upgrade naar v5: is_recurring
             db.execSQL("ALTER TABLE " + TABEL_TRANSACTIES + " ADD COLUMN " + KOLOM_IS_RECURRING + " INTEGER DEFAULT 0");
        }
    }
}
