// 📁 data/remote/firebase/StorageManager.kt
package com.example.luontopeli.data.remote.firebase

/**
 * Offline-tilassa toimiva tallennushallinta (stub).
 *
 * Kuvat säilytetään laitteen paikallisessa tallennustilassa.
 * Firebase Storage vaatisi Blaze-tilille päivittämisen (luottokortti),
 * joten kuvien pilvitallennus on jätetty pois.
 *
 * Löytöjen metadata (nimi, sijainti, kasvilaji) tallennetaan
 * Firestoreen — kuvat pysyvät paikallisesti.
 */
class StorageManager {

    /**
     * Palauttaa paikallisen tiedostopolun pilvi-URL:n sijaan.
     * Kuva on jo tallennettu laitteelle CameraViewModelissa.
     */
    suspend fun uploadImage(localFilePath: String, spotId: String): Result<String> {
        return Result.success(localFilePath)
    }

    /** Ei tee mitään — kuvat ovat vain paikallisesti. */
    suspend fun deleteImage(spotId: String): Result<Unit> {
        return Result.success(Unit)
    }
}