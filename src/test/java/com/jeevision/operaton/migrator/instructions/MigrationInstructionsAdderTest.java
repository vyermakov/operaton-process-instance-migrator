package com.jeevision.operaton.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.operaton.bpm.engine.migration.MigrationInstruction;
import org.operaton.bpm.engine.migration.MigrationPlan;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MigrationInstructionsAdderTest {

    private static final String ACTIVITY_1 = "ServiceTask1";
    private static final String ACTIVITY_2 = "ServiceTask2";
    private static final String ACTIVITY_3 = "UserTask1";
    private static final String ACTIVITY_4 = "UserTask2";
    private static final String ACTIVITY_5 = "ReceiveTask1";
    private static final String ACTIVITY_6 = "ReceiveTask2";

    @Mock
    MigrationPlan migrationPlan;

    // Use a real ArrayList that delegates all operations; we capture the
    // addAll() argument via ArgumentCaptor to verify what was passed in.
    TrackingList<MigrationInstruction> migrationPlanInstructionList;

    List<MigrationInstruction> instructionList;

    @Mock
    MigrationInstruction migrationInstruction1;
    @Mock
    MigrationInstruction migrationInstruction2;
    @Mock
    MigrationInstruction migrationInstruction3;
    @Mock
    MigrationInstruction migrationInstruction4;

    @Captor
    private ArgumentCaptor<List<MigrationInstruction>> migrationInstructionCaptor;

    @BeforeEach()
    void setUp() {
        migrationPlanInstructionList = new TrackingList<>();
        migrationPlanInstructionList.add(migrationInstruction1);
        migrationPlanInstructionList.add(migrationInstruction2);

        instructionList = new ArrayList<>();
        instructionList.add(migrationInstruction3);
        instructionList.add(migrationInstruction4);

        when(migrationPlan.getInstructions()).thenReturn(migrationPlanInstructionList);
    }

    @Test
    void addInstructions_should_overwrite_instructions_with_existing_source() {
        when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_1);

        when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_2);

        when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_3);

        when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_4);

        MigrationInstructionsAdder.addInstructions(migrationPlan, instructionList);

        assertThat(migrationPlanInstructionList.cleared).isTrue();
        assertThat(migrationPlanInstructionList.addAllCapture).isNotNull();
        List<MigrationInstruction> newInstructions = migrationPlanInstructionList.addAllCapture;

        assertThat(newInstructions).hasSize(2);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_1
                && instruction.getTargetActivityId() == ACTIVITY_3);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_2
                && instruction.getTargetActivityId() == ACTIVITY_4);
    }

    @Test
    void addInstructions_should_add_instructions_with_not_yet_existing_source() {
        when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_1);

        when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_2);

        when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_3);
        when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_4);

        when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_5);
        when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_6);

        MigrationInstructionsAdder.addInstructions(migrationPlan, instructionList);

        assertThat(migrationPlanInstructionList.cleared).isTrue();
        assertThat(migrationPlanInstructionList.addAllCapture).isNotNull();
        List<MigrationInstruction> newInstructions = migrationPlanInstructionList.addAllCapture;

        assertThat(newInstructions).hasSize(4);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_1
                && instruction.getTargetActivityId() == ACTIVITY_1);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_2
                && instruction.getTargetActivityId() == ACTIVITY_2);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_3
                && instruction.getTargetActivityId() == ACTIVITY_4);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_5
                && instruction.getTargetActivityId() == ACTIVITY_6);
    }

    /**
     * Wraps an ArrayList to capture calls to clear() and addAll() so we can verify them
     * without needing to mock the ArrayList directly (which fails on newer JDKs).
     */
    static class TrackingList<T> implements List<T> {
        private final List<T> delegate = new ArrayList<>();
        boolean cleared = false;
        List<T> addAllCapture;

        @Override
        public int size() { return delegate.size(); }

        @Override
        public boolean isEmpty() { return delegate.isEmpty(); }

        @Override
        public boolean contains(Object o) { return delegate.contains(o); }

        @Override
        public Iterator<T> iterator() { return delegate.iterator(); }

        @Override
        public Object[] toArray() { return delegate.toArray(); }

        @Override
        public <T1> T1[] toArray(T1[] a) { return delegate.toArray(a); }

        @Override
        public boolean add(T t) { return delegate.add(t); }

        @Override
        public boolean remove(Object o) { return delegate.remove(o); }

        @Override
        public boolean containsAll(Collection<?> c) { return delegate.containsAll(c); }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            addAllCapture = new ArrayList<>(c);
            return delegate.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) { return delegate.addAll(index, c); }

        @Override
        public boolean removeAll(Collection<?> c) { return delegate.removeAll(c); }

        @Override
        public boolean retainAll(Collection<?> c) { return delegate.retainAll(c); }

        @Override
        public void clear() {
            cleared = true;
            delegate.clear();
        }

        @Override
        public T get(int index) { return delegate.get(index); }

        @Override
        public T set(int index, T element) { return delegate.set(index, element); }

        @Override
        public void add(int index, T element) { delegate.add(index, element); }

        @Override
        public T remove(int index) { return delegate.remove(index); }

        @Override
        public int indexOf(Object o) { return delegate.indexOf(o); }

        @Override
        public int lastIndexOf(Object o) { return delegate.lastIndexOf(o); }

        @Override
        public ListIterator<T> listIterator() { return delegate.listIterator(); }

        @Override
        public ListIterator<T> listIterator(int index) { return delegate.listIterator(index); }

        @Override
        public List<T> subList(int fromIndex, int toIndex) { return delegate.subList(fromIndex, toIndex); }
    }
}
