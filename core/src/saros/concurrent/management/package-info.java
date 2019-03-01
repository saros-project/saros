/**
 *
 *
 * <h1>Management Overview</h1>
 *
 * The Management package manages {@link JupiterServer} and {@link JupiterClient} instances and all
 * Jupiter activities. Local Operations on each client site will be transformed into
 * JupiterActivities (by {@link ConcurrentDocumentClient}) and sent to the host ({@link
 * ConcurrentDocumentServer}). The JupiterActivity will be transformed into its corresponding
 * operation and sent to all clients. This process has to be atomic and may not be interrupted.
 *
 * <p>The {@link ConcurrentDocumentServer} exists only on the host site of a Saros session, even
 * when a client adds resources to this session.
 */
package saros.concurrent.management;
