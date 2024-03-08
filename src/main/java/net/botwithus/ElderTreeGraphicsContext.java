package net.botwithus;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class ElderTreeGraphicsContext extends ScriptGraphicsContext {

    private ElderTree script;
    private int startingXP;
    private long scriptstartTime;
    boolean isScriptRunning = false;

    public ElderTreeGraphicsContext(ScriptConsole scriptConsole, ElderTree script) {
        super(scriptConsole);
        this.script = script;
        this.startingXP = Skills.WOODCUTTING.getSkill().getExperience();
        this.scriptstartTime = System.currentTimeMillis();
    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("Elder Tree Farming", ImGuiWindowFlag.None.getValue())) {

            long elapsedTimeMillis = System.currentTimeMillis() - scriptstartTime;
            long elapsedSeconds = elapsedTimeMillis / 1000;
            long hours = elapsedSeconds / 3600;
            long minutes = (elapsedSeconds % 3600) / 60;
            long seconds = elapsedSeconds % 60;

            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Welcome!");
                    ImGui.Text("My scripts state is: " + script.getBotState());
                    if (ImGui.Button("Start")) {
                        //button has been clicked
                        script.setBotState(ElderTree.BotState.SKILLING);
                    }
                    ImGui.SameLine();
                    if (ImGui.Button("Stop")) {
                        //has been clicked
                        script.setBotState(ElderTree.BotState.IDLE);
                    }
                    ImGui.EndTabItem();
                }
                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                    String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    ImGui.SeparatorText("Time Running  " + displayTimeRunning);
                    ImGui.Text("Current WoodCutting Level: " + Skills.WOODCUTTING.getLevel());
                    displayXPGained(Skills.WOODCUTTING);
                    displayXpPerHour(Skills.WOODCUTTING);
                    XPtillNextLevel(Skills.WOODCUTTING);
                    String timetolevel = calculateTimeTillNextLevel();
                    ImGui.Text(timetolevel);
                    ImGui.Separator();
                    ImGui.Text("Logs:" + script.logs);
                    ImGui.Text("Logs Per Hour: " + script.logsperhour);
                    ///Number of logs per hour
                    ImGui.Separator();
                    //script.setSomeBool(ImGui.Checkbox("Nothing to see here", script.isSomeBool()));
                    ImGui.EndTabItem();
                }
                ImGui.EndTabBar();
            }
            ImGui.End();
        }

    }


    private void displayXPGained(Skills skill)
    {
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - startingXP;
        ImGui.Text("Xp Gained: " + xpGained);
    }

    private void displayXpPerHour(Skills skill)
    {
        long timeelapsed   = System.currentTimeMillis() - scriptstartTime;
        double hourElapsed = timeelapsed / (1000.0 * 60 * 60);
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - startingXP;
        double xpPerHour = hourElapsed >0 ? xpGained/hourElapsed :0;

        String forXpPerHour = formatNumberForDisplay(xpPerHour);  //formatted xp per hour
        ImGui.Text( "XP Per Hour: " + forXpPerHour );

    }

    private String formatNumberForDisplay(double number) {
        if (number < 1000) {
            return String.format("%.0f", number); // No suffix
        } else if (number < 1000000) {
            return String.format("%.1fk", number / 1000); // Thousands
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000); // Millions
        } else {
            return String.format("%.1fB", number / 1000000000); // Billions
        }
    }

    private void XPtillNextLevel(Skills skill) {

        // Get the current XP in RUNECRAFTING
        int currentXP = Skills.WOODCUTTING.getSkill().getExperience();
        // Get the current level in RUNECRAFTING
        int currentLevel = Skills.WOODCUTTING.getSkill().getLevel();
        // Calculate the XP required for the next level
        int xpForNextLevel = Skills.WOODCUTTING.getExperienceAt(currentLevel + 1);
        // Calculate the difference between the XP required for the next level and the current XP
        int xpTillNextLevel = xpForNextLevel - currentXP;

        ImGui.Text("XP Till Next Level: " + xpTillNextLevel);
    }

    private String calculateTimeTillNextLevel() {
        int currentXP = Skills.WOODCUTTING.getSkill().getExperience();
        int currentLevel = Skills.WOODCUTTING.getSkill().getLevel();
        int xpForNextLevel = Skills.WOODCUTTING.getExperienceAt(currentLevel + 1);
        int xpForCurrentLevel = Skills.WOODCUTTING.getExperienceAt(currentLevel);
        int xpGainedTowardsNextLevel = currentXP - xpForCurrentLevel;

        long currentTime = System.currentTimeMillis();
        int xpGained = currentXP - startingXP;
        long timeElapsed = currentTime - scriptstartTime; // Time elapsed since tracking started in milliseconds

        if (xpGained > 0 && timeElapsed > 0) {
            // Calculate the XP per millisecond
            double xpPerMillisecond = xpGained / (double) timeElapsed;
            // Estimate the time to level up in milliseconds
            long timeToLevelMillis = (long) ((xpForNextLevel - currentXP) / xpPerMillisecond);

            // Convert milliseconds to hours, minutes, and seconds
            long timeToLevelSecs = timeToLevelMillis / 1000;
            long hours = timeToLevelSecs / 3600;
            long minutes = (timeToLevelSecs % 3600) / 60;
            long seconds = timeToLevelSecs % 60;

            return String.format("Time to level: %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "Time to level: calculating...";
        }
    }

    private void displayTimeRunning() {
        long elapsedTimeMillis = System.currentTimeMillis() - scriptstartTime;
        long elapsedSeconds = elapsedTimeMillis / 1000;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        String timeRunningFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        ImGui.Text(timeRunningFormatted);
    }
    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
