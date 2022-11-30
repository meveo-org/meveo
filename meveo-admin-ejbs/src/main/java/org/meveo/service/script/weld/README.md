# CDI (Weld) integration

In order to enable the injection of libraries and scripts into a script, we had to 
use a bean manager to instantiate our scripts. However, the implementation provided 
by Weld (BeanManagerImpl) is not very extensible, so we had to use reflection to replace 
some of its components for suiting our needs.

## Bean definition

When a script is compiled, and we need to load it in cache to execute it, then we need 
to create a corresponding Bean, this is achieved by calling [MeveoBeanManager](./MeveoBeanManager.java) 
*createBean* and *addBean* methods

## Bean instantiation

To execute a script, we call [MeveoBeanManager#getInstance(Bean)](./MeveoBeanManager.java), 
then call the *execute* method of ``ScriptInterface``.

## Replacements in Weld implementation

As we can see in the constructor of MeveoBeanManager, some fields of the base BeanManager 
we use are replaced by custom ones

### ClientProxyProvider

When injecting beans, CDI instantiates proxies of the actual classes, but it requires 
to use the JBOSS ModuleClassLoader, however we use an UrlClassLoader to load our scripts. 
The [MeveoProxyProvider](./MeveoProxyProvider.java) returns an instance of the requested 
type instead of returning a proxy. This behavior might cause some problem for some more 
advanced usage, but we will see that on time.

### BeanResolver

The normal flow for WELD is to define all its beans at startup, and once it is fully 
initialized we are not supposed to modify anything. The [MeveoBeanResolver](./MeveoBeanResolver) 
solves this problems by allowing addition and deletion of beans at runtime. It also 
modifies the lookup behavior by comparing bean using the java type names instead of 
the classes objects.

## Validator service

The modifications are quite the same than for the bean resolver, in the way that it 
uses java type names for bean lookup.

## Injection target service

