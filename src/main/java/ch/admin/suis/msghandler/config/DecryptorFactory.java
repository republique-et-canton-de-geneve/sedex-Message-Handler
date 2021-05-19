package ch.admin.suis.msghandler.config;

import ch.admin.suis.msghandler.common.ClientCommons;
import ch.glue.fileencryptor.CipherFactory;
import ch.glue.fileencryptor.Decryptor;
import ch.glue.fileencryptor.DecryptorConfiguration;
import ch.glue.fileencryptor.KeyStoreFactory;
import ch.glue.fileencryptor.PrivateKeyFactory;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.io.File;

/**
 * Factory to create decryptor.
 *
 * @author $Author$
 * @version $Revision$
 */

public class DecryptorFactory
{
  private final String workingDir;

  /**
   * Creates a new factory.
   *
   * @param workingDir
   *     the working directory of the MessageHandler
   */
  public DecryptorFactory(final String workingDir)
  {
    this.workingDir = workingDir;
  }

  /**
   * Creates a new instance of decryptor from this configuration.
   *
   * @return a new instance
   */
  public Decryptor create(final HierarchicalConfiguration decryptorConfig)
  {
    final String algorithm = decryptorConfig.getString("[@algorithm]");
    final String keystore = decryptorConfig.getString("[@keystore]");
    final String password = decryptorConfig.getString("[@password]");
    final String certificateAlias = decryptorConfig.getString("[@certificateAlias]");

    final PrivateKeyFactory privateKeyFactory = KeyStoreFactory.forPkcs12(new File(keystore), certificateAlias,
        password);

    final DecryptorConfiguration decryptorConfiguration = new DecryptorConfiguration.Builder()
        .setTargetDirectory(new File(workingDir, ClientCommons.INBOX_TMP_DIR))
        .setPrivateKeyFactory(privateKeyFactory)
        .setCipherFactory(CipherFactory.forAlgorithm(algorithm))
        .build();

    return new Decryptor(decryptorConfiguration);
  }
}
