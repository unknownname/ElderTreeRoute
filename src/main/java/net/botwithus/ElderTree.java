package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.api.game.hud.traversal.Lodestone;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.InventoryUpdateEvent;
import net.botwithus.rs3.game.*;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.movement.TraverseEvent;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.*;
import net.botwithus.rs3.util.Regex;


import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

public class ElderTree extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();
    public long scriptStartTime = System.currentTimeMillis();
    private Pattern woodboxPattern = Regex.getPatternForContainingOneOf("Wood box", "wood box");
    private Pattern logPattern = Regex.getPatternForContainingOneOf("Logs", "logs");
    public int logs =0;
    public   int logsperhour =0;

    private Area VarRock = new Area.Rectangular(new Coordinate(3236, 3371,0), new Coordinate(3234, 3364,0));
    private Area VarrockTree = new Area.Rectangular(new Coordinate(3256,3372,0), new Coordinate(3258,3367,0));
    private Area FortTree = new Area.Rectangular(new Coordinate(3375,3545,0), new Coordinate(3373,3540,0));
    private Area FaladorTree = new Area.Rectangular(new Coordinate(3061,3318,0), new Coordinate(3059,3315,0));
    private Area Fort = new Area.Rectangular(new Coordinate(3347,3542,0), new Coordinate(3351,3547,0));
    private Area Falador2 = new Area.Rectangular(new Coordinate(3061,3318,0), new Coordinate(3059,3315,0));

    private Area Falador1 = new Area.Rectangular(new Coordinate(3077,3321,0), new Coordinate(3081,3327,0));
    enum BotState {
        //define your own states here
        IDLE,
        SKILLING,
        BANKING,
        //...
    }

    public ElderTree(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new ElderTreeGraphicsContext(getConsole(), this);

        subscribe(InventoryUpdateEvent.class, inventoryUpdateEvent -> {
            Item item = inventoryUpdateEvent.getNewItem();
            if(item != null)
            {
                if(item.getInventoryType().getId() != 93)
                {
                    return;
                }
                String itemName = item.getName();
                if(itemName != null)
                {
                    if(itemName.equalsIgnoreCase("Elder logs")){
                        logs = logs+ item.getStackSize();
                        println("logs" + logs);
                    }
                }
            }
            long currenttime = (System.currentTimeMillis() -scriptStartTime)/1000;
            logsperhour = (int) (Math.round(3600.0 /currenttime *logs));
            println("logs per hour:" + logsperhour);

        });
    }


    @Override
    public void onLoop() {
        //Loops every 100ms by default, to change:
        //this.loopDelay = 500;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            //wait some time so we dont immediately start on login.
            Execution.delay(random.nextLong(1000, 2200));
            return;
        }
        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000, 3000));
            }
            case SKILLING -> {
                //do some code that handles your skilling
                Execution.delay(handleSkilling(player));

            }
            case BANKING -> {
                //handle your banking logic, etc
                Execution.delay(handleBanking(player));
            }
        }
    }

    private long handleBanking(LocalPlayer player) {

        //println("Anim id: " + player.getAnimationId());
        println("Player moving: " + player.isMoving());
        if (player.isMoving()) {
            return random.nextLong(1000,2000);
        }

        if (Bank.isOpen()) {
            println("Bank is open!");
            ResultSet<Item> logsInBox = InventoryItemQuery.newQuery(937).results();
            if (logsInBox.stream().anyMatch(item -> item.getId() != -1)) {
                //deposit logs from box
                Component box = ComponentQuery.newQuery(517).componentIndex(15).option("Empty - logs and bird's nests").results().first();
                if (box != null) {
                    boolean success = box.interact("Empty - logs and bird's nests");
                    println("Deposited box contents: " + success);
                    Bank.depositAllExcept(54913);
                    if (success)
                        return random.nextLong(750, 1000);
                }
            } else {
                //either box was empty or we deposited
                println("Already deposited, skipping.");
            }

            Component logsForDeposit = ComponentQuery.newQuery(517).componentIndex(15).option("Deposit-All").results().first();
            if (logsForDeposit != null) {
                boolean success = logsForDeposit.interact("Deposit-All");
                println("Tried to deposit log: " + success);
                if (success) {
                    return random.nextLong(1500, 2000);
                }
            }
            //we can go back to our skill state
            botState = BotState.SKILLING;

        }
        else {
            if(player.getCoordinate().getRegionId() != 13111) {
                Lodestone.FORT_FORINTHRY.teleport();
            }else {

                Npc copperport = NpcQuery.newQuery().name("Copperpot").results().nearest();
                if(copperport != null)
                {
                    println("We found the Bank");
                    println("Interact Bank" + copperport.interact("Bank"));
                }
            }
            /*SceneObject BankChest = SceneObjectQuery.newQuery().name("Bank chest").id(124899).option("Bank").results().nearest();
                if (BankChest != null) {
                    println("Yay, we found our bank.");
                    println("Interacted bank: " + BankChest.interact("Bank"));
                }*/


        }

        return random.nextLong(750, 1500);
    }

    private void fillBox(Item woodbox) {
        Component woodboxComp = ComponentQuery.newQuery(1473).componentIndex(5).itemName(woodbox.getName()).option("Fill").results().first();
        if (woodboxComp != null) {
            println("Filled woodbox: " + woodboxComp.interact("Fill"));
        }
    }
    private long handleSkilling(LocalPlayer player) {
        //for example, if skilling progress interface is open, return a randomized value to keep waiting.

        if (Backpack.isFull()) {

            Item woodbox = InventoryItemQuery.newQuery(93).name(woodboxPattern).results().first();
            if (woodbox == null || woodbox.getId() == -1) {
                println("We did not find our woodox, so we should bank.");
                botState = BotState.BANKING;
            } else {
                //we found our woodbox
                println("Yay, found found our woodbox: " + woodbox.getName());

                //TODO refactor this into a function so that its not repeated in handleBanking.
                //do calcs
                if (woodbox.getName() != null) {
                    int capacity = getBaseWoodboxCapacity(woodbox.getName());
                    capacity = capacity + getAdditionalWoodboxCapacity();
                    println("Our expected capacity is: " + capacity);
                    Item logs = InventoryItemQuery.newQuery(93).name(logPattern).results().first();
                    if (logs == null && logs.getId() == -1 && logs.getName() == null) {
                        println("No log found in inventory.");
                    } else {
                        //we found the log, and can proceed
                        Item logsStored = InventoryItemQuery.newQuery(937).name(logs.getName()).results().first();
                        if (logsStored == null || logsStored.getId() == -1) {
                            println("We didnt find logs in the woodbox, but we have one, so fill it.");
                        } else {
                            //good to finally fill if math maths
                            if (logsStored.getStackSize() >= capacity) {
                                //we cant fill, our woodbox is full, and we should actually bank
                                println("Moving to banking state");
                                botState = BotState.BANKING;
                                return random.nextLong(1500,3000);
                            }
                        }
                        //we can fill our box
                        Component woodboxComp = ComponentQuery.newQuery(1473).componentIndex(5).itemName(woodbox.getName()).option("Fill").results().first();
                        if (woodboxComp != null) {
                            println("Filled woodbox: " + woodboxComp.interact("Fill"));
                        }
                    }
                }
            }
            return random.nextLong(1500,3000);
        }

        println("Anim id: " + player.getAnimationId());
        println("Player moving: " + player.isMoving());
        if (player.getAnimationId() != -1 || player.isMoving()) {
            return random.nextLong(1000,2000);
        }
        if(VarManager.getVarbitValue(52992) == 3) {
            if (Movement.traverse(NavPath.resolve(FortTree.getRandomWalkableCoordinate())) == TraverseEvent.State.FINISHED) {
                SceneObject tree1 = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();

                if (VarManager.getVarbitValue(52992) == 15) {
                    println("Teleporting to VarRock");
                    //Movement.traverse(NavPath.resolve(VarrockTree.getRandomWalkableCoordinate()));
                    Lodestone.VARROCK.teleport();
                } else if (tree1 != null) {
                    println("Interacted tree: " + tree1.interact("Chop down"));
                }
            } else if (VarManager.getVarbitValue(20603) == 0) {
                if (Movement.traverse(NavPath.resolve(VarrockTree.getRandomWalkableCoordinate())) == TraverseEvent.State.FINISHED) {
                    SceneObject tree = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
                    if (VarManager.getVarbitValue(20603) == 1) {
                        println("Teleporting to Draynor Village");
                        Lodestone.DRAYNOR_VILLAGE.teleport();
                        //Movement.traverse(NavPath.resolve(FaladorTree.getRandomWalkableCoordinate()));
                    } else if (tree != null) {
                        println("Interacted tree: " + tree.interact("Chop down"));
                    }
                } else if (VarManager.getVarbitValue(20600) == 0) {
                    if (Movement.traverse(NavPath.resolve(FaladorTree.getRandomWalkableCoordinate())) == TraverseEvent.State.FINISHED) {
                        SceneObject tree2 = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
                        if (VarManager.getVarbitValue(20600) == 1) {
                            Lodestone.FORT_FORINTHRY.teleport();
                            println("Teleporting to Fort Elder Tree");
                        } else if (tree2 != null) {
                            println("Interacted tree: " + tree2.interact("Chop down"));
                        }
                    }
                }
            }
        }













        /*if(player.getCoordinate() != VarrockTree.getRandomCoordinate())   //Varrock Tree
        {
            if(Movement.traverse(NavPath.resolve(VarrockTree.getRandomWalkableCoordinate())) == TraverseEvent.State.FINISHED)
            {
                SceneObject tree = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
                if(VarManager.getVarbitValue(20603) == 1)
                {
                    Lodestone.DRAYNOR_VILLAGE.teleport();
                    println("Teleporting to Draynor Village");
                }
                else if (tree != null) {
                    println("Interacted tree: " + tree.interact("Chop down"));
                }
            }
        }*/

       /* if(player.getCoordinate().getRegionId() != 12083) //Falador
        {
            if(Movement.traverse(NavPath.resolve(FaladorTree.getRandomWalkableCoordinate())) == TraverseEvent.State.FINISHED)
            {
                SceneObject tree2 = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
                if(VarManager.getVarbitValue(20600) == 1)
                {
                    Lodestone.FORT_FORINTHRY.teleport();
                    println("Teleporting to Fort Elder Tree");
                }
                else if (tree2 != null) {
                    println("Interacted tree: " + tree2.interact("Chop down"));
                }
            }
        }*/

/*        if(player.getCoordinate().getRegionId() == 13111)

        {
            //Move to Fort Elder Tree area
            Coordinate FortRandom = Fort.getRandomCoordinate();
            Travel.walkTo(FortRandom.getX(),FortRandom.getY());
            Execution.delayUntil(10000,() -> {
               assert player !=null;
               return Fort.contains(player.getCoordinate());
            });
           *//* SceneObject move = SceneObjectQuery.newQuery().name("Fairy ring ").results().nearest();
            if (move != null) {
                println("Interacted tree: " + move.interact("Use"));
            }*//*
        }
        if(player.getCoordinate().getRegionId() == 13367)  // Fort
        {
            SceneObject tree1 = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
            if(VarManager.getVarbitValue(52992) == 15)
            {
                Lodestone.VARROCK.teleport();
                println("Teleporting to VarRock");
            }
            if (tree1 != null) {
                println("Interacted tree: " + tree1.interact("Chop down"));
            }
        }*/

    /*    if(player.getCoordinate().getRegionId() == 12339)
        {    if(Movement.traverse(NavPath.resolve(FortTree.getRandomWalkableCoordinate())) == TraverseEvent.State.FINISHED){

        }
            Coordinate Falador1Random = Falador1.getRandomCoordinate();
            Travel.walkTo(Falador1Random.getX(),Falador1Random.getY());
            Execution.delayUntil(10000,() -> {
                assert player !=null;
                return Falador1.contains(player.getCoordinate());
            });

            Coordinate Falador2Radndom = Falador2.getRandomCoordinate();
            Travel.walkTo(Falador2Radndom.getX(),Falador2Radndom.getY());
            Execution.delayUntil(10000,() -> {
                assert player !=null;
                return Falador2.contains(player.getCoordinate());
            });
        }*/

       /* if(player.getCoordinate().getRegionId() == 12083)   //Draynor Elder Tree
        {
            SceneObject tree2 = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
            if(VarManager.getVarbitValue(20600) == 1)
            {
                Lodestone.FORT_FORINTHRY.teleport();
                println("Teleporting to Fort Elder Tree");
            }
            else if (tree2 != null) {
                println("Interacted tree: " + tree2.interact("Chop down"));
            }
        }*/


      /*  if(player.getCoordinate().getRegionId() == 12852)   // Varrock
        {
            SceneObject tree = SceneObjectQuery.newQuery().name("Elder tree").option("Chop down").hidden(false).results().nearest();
            if(tree == null)
            {
                Coordinate VarRockRandom = VarRock.getRandomCoordinate();
                Travel.walkTo(VarRockRandom.getX(),VarRockRandom.getY());
                Execution.delayUntil(10000,() -> {
                    assert player !=null;
                    return VarRock.contains(player.getCoordinate());
                });
            }
            else if(VarManager.getVarbitValue(20603) == 1)
            {
                Lodestone.DRAYNOR_VILLAGE.teleport();
                println("Teleporting to Draynor Village");
            }
            else if (tree != null) {
                println("Interacted tree: " + tree.interact("Chop down"));
            }

        }*/
        return random.nextLong(1500, 3000);
    }

    private void movetoVarrock()
    {

    }

    public int getAdditionalWoodboxCapacity() {
        int level = Skills.WOODCUTTING.getActualLevel();
        for (int threshold = 95; threshold > 0; threshold -= 10) {
            if (level >= threshold)
                return threshold + 5;
        }
        return 0;
    }
    public int getBaseWoodboxCapacity(String woodboxName) {
        switch (woodboxName) {
            case "Wood box":
                return 70;
            case "Oak wood box":
                return 80;
            case "Willow wood box":
                return 90;
            case "Teak wood box":
                return 100;
            case "Maple wood box":
                return 110;
            case "Acadia wood box":
                return 120;
            case "Mahogany wood box":
                return 130;
            case "Yew wood box":
                return 140;
            case "Magic wood box":
                return 150;
            case "Elder wood box":
                return 160;
        }
        return 0;
    }


    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}
