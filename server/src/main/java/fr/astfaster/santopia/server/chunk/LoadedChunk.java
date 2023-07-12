package fr.astfaster.santopia.server.chunk;

import fr.astfaster.santopia.api.guild.claim.SantopiaClaim;

public class LoadedChunk {

    private final int x;
    private final int z;
    private SantopiaClaim claim;

    public LoadedChunk(int x, int z, SantopiaClaim claim) {
        this.x = x;
        this.z = z;
        this.claim = claim;
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

    public SantopiaClaim claim() {
        return this.claim;
    }

    public void claim(SantopiaClaim claim) {
        this.claim = claim;
    }

    public boolean claimed() {
        return this.claim != null;
    }

}
