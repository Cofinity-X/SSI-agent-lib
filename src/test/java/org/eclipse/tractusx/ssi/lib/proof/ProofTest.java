package org.eclipse.tractusx.ssi.lib.proof;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.eclipse.tractusx.ssi.lib.model.proof.Proof;
import org.junit.jupiter.api.Test;

public class ProofTest {

  @Test
  void shouldThrowExceptionWhenProofPurposeMissing() {
    assertThrows(IllegalArgumentException.class, () -> new Proof(Map.of(Proof.TYPE, "foo")));
  }

  @Test
  void shouldThrowExceptionWhenProofPurposeEmpty() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Proof(Map.of(Proof.TYPE, "foo", Proof.PROOF_PURPOSE, "")));
  }

  @Test
  void shouldCreateProof() {
    assertDoesNotThrow(() -> new Proof(Map.of(Proof.TYPE, "foo", Proof.PROOF_PURPOSE, "bar")));
  }
}
