![honeycomb-png-transparent-clipart-freeuse](https://user-images.githubusercontent.com/40205361/70325973-3e884600-182b-11ea-9977-692f9bc1dbfd.png)



1. [Introduction](#introduction)
2. [Quick Start Guide](#quick-start-guide)
3. [Running The Web Server](#running-the-web-server)
4. [Configuring Your CorDapp](#configuring-your-cordapp)
5. [Testing Your CorDapp](#testing-your-cordapp)
6. [Getting Template Updates](#getting-template-updates)
7. [Known Problems](#known-problems)
8. [Contribution](#contribution)


# Honeycomb Exchange CorDapp

# Introduction

This CorDapp contains a series of flows and contracts including a transaction mechanism for a multi-asset and multi-instrument DeX. 

   - A liquid Secondary Market for currently illiquid or alternative assets e.g. Fine Art, Commercial Real Estate, Shipping, Aviation, Private Placements
   - Transaction mechanism for a multi-asset and multi-instrument DeX
   - Introduce Asset Lock Pattern which addresses
    
        - Settlement Risk
        - Credit Risk 
        - Fat Finger errors
        
   - Enable fully Decentralised P2P Trading with privacy by taking advatage of the token receipt pattern
      
#Cordapp Design 

This is the CorDapp design diagram mapped our using Corda Design Language. 

Here we can see the 3 step process in performing an atomic exchange or asset and on-ledger cash tokens in a way to preserves transactional privacy downstream.

1. `LockAssetTransaction` - the asset owner or seller locks the asset by consuming it an transforming to status LOCKED while marking all the information needed for the buyer to provide valid payment and claim the asset for their own
2. `PaymentTransaction` - the transfer to cash tokens from  buyer to seller and the creation of a receipt state that contractually matches the value of tokens transferred,  to whom and from whom. This receipt is used to the subequent transaction to claim ownership of the asset
3. `UnlockAssetTransaction` - now the receipt can be spend by the buyer to claim ownership of the asset. This involves changing the lock status to unlocked and chaning the owner identity to be the buyers.

![image](https://user-images.githubusercontent.com/40205361/70395623-6bd61f00-19f8-11ea-9779-960e489be7f8.png)


# Quick Start Guide

If you're continuing from the terminal, you can go ahead and build some nodes.

```
cd Honeycomb
./gradlew deployNodes
./build/nodes/runnodes
```


